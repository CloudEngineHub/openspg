/*
 * Copyright 2023 OpenSPG Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 */

package com.antgroup.openspg.reasoner.thinker.engine;

import com.antgroup.openspg.reasoner.thinker.TripleStore;
import com.antgroup.openspg.reasoner.thinker.logic.LogicNetwork;
import com.antgroup.openspg.reasoner.thinker.logic.Result;
import com.antgroup.openspg.reasoner.thinker.logic.graph.Element;
import com.antgroup.openspg.reasoner.thinker.logic.graph.Entity;
import com.antgroup.openspg.reasoner.thinker.logic.graph.Node;
import com.antgroup.openspg.reasoner.thinker.logic.graph.Triple;
import com.antgroup.openspg.reasoner.thinker.logic.rule.ClauseEntry;
import com.antgroup.openspg.reasoner.thinker.logic.rule.Rule;
import com.antgroup.openspg.reasoner.thinker.logic.rule.TreeLogger;
import com.antgroup.openspg.reasoner.thinker.logic.rule.visitor.RuleExecutor;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class InfGraph implements Graph {
  private LogicNetwork logicNetwork;
  private TripleStore tripleStore;
  private GraphStore graphStore;

  public InfGraph(LogicNetwork logicNetwork, GraphStore graphStore) {
    this.logicNetwork = logicNetwork;
    this.tripleStore = new MemTripleStore();
    this.graphStore = graphStore;
  }

  @Override
  public void init(Map<String, String> param) {
    this.graphStore.init(param);
  }

  @Override
  public List<Result> find(Triple pattern, Map<String, Object> context) {
    List<Result> result = new LinkedList<>();
    prepareContext(context);
    // Step1: find pattern in graph
    List<Result> dataInGraph = graphStore.find(pattern, context);
    if (CollectionUtils.isNotEmpty(dataInGraph)) {
      for (Result tri : dataInGraph) {
        addTriple((Triple) tri.getData());
        result.add(tri);
      }
    }
    result.addAll(inference(pattern, context));
    return result;
  }

  @Override
  public List<Result> find(Node s, Map<String, Object> context) {
    prepareContext(context);
    return inference(s, context);
  }

  private void prepareContext(Map<String, Object> context) {
    if (context != null) {
      for (Object val : context.values()) {
        if (val instanceof Entity) {
          addEntity((Entity) val);
        }
      }
    }
  }

  private List<Result> inference(Element pattern, Map<String, Object> context) {
    List<Result> rst = new LinkedList<>();
    for (Rule rule : logicNetwork.getBackwardRules(pattern)) {
      List<Element> body =
          rule.getBody().stream().map(ClauseEntry::toElement).collect(Collectors.toList());
      List<List<Result>> data = prepareElements(body, rule.getHead().toElement(), pattern, context);
      if (CollectionUtils.isEmpty(data)) {
        continue;
      }
      for (List<Result> d : data) {
        TreeLogger traceLogger = new TreeLogger(rule.getRoot().toString());
        Boolean ret =
            rule.getRoot()
                .accept(
                    d.stream().map(Result::getData).collect(Collectors.toList()),
                    context,
                    new RuleExecutor(),
                    traceLogger);
        if (ret) {
          Element ele = rule.getHead().toElement();
          rst.add(new Result(ele, traceLogger));
          if (ele instanceof Triple) {
            addTriple((Triple) ele);
          } else {
            addEntity((Entity) ele);
          }
        }
      }
    }
    return rst;
  }

  private List<List<Result>> prepareElements(
      List<Element> body, Element head, Element pattern, Map<String, Object> context) {
    List<List<Result>> elements = new ArrayList<>();
    Set<Element> choose = new HashSet<>();
    Queue<Element> starts = new LinkedBlockingDeque<>();
    Element start = getStart(pattern, head);
    starts.add(start);
    while (choose.size() < body.size()) {
      Element s = starts.poll();
      for (Element e : body) {
        if (choose.contains(e)) {
          continue;
        }
        if (e instanceof Entity) {
          choose.add(e);
          if (CollectionUtils.isEmpty(elements)) {
            elements.add(new LinkedList<>(prepareElement(e, context)));
          } else {
            for (List<Result> evidence : elements) {
              evidence.addAll(prepareElement(e, context));
            }
          }
        } else {
          Triple t = (Triple) e;
          if (t.getSubject().alias().equals(s.alias())) {
            starts.add(t.getObject());
          } else if (t.getObject().alias().equals(s.alias())) {
            starts.add(t.getSubject());
          } else {
            continue;
          }
          choose.add(e);
          if (CollectionUtils.isEmpty(elements)) {
            Triple triple = buildTriple(null, s, t);
            elements.addAll(prepareElement(null, triple, context));
          } else {
            List<List<Result>> tmpElements = new LinkedList<>();
            for (List<Result> evidence : elements) {
              Triple triple = buildTriple(evidence, s, t);
              if (triple != null) {
                tmpElements.addAll(prepareElement(evidence, triple, context));
              }
            }
            elements.clear();
            elements = tmpElements;
          }
        }
      }
    }
    return elements;
  }

  private Triple buildTriple(List<Result> evidence, Element s, Triple triple) {
    Entity entity = null;
    if (CollectionUtils.isEmpty(evidence)) {
      entity = (Entity) s;
    } else {
      for (Result r : evidence) {
        Element e = r.getData();
        if (e instanceof Entity && e.alias() == s.alias()) {
          entity = (Entity) r.getData();
        } else if (e instanceof Triple
                && ((Triple) e).getSubject().alias() == s.alias()) {
          entity = (Entity) ((Triple) e).getSubject();
        } else if (e instanceof Triple
                && ((Triple) e).getObject().alias() == s.alias()) {
          entity = (Entity) ((Triple) e).getObject();
        }
      }
    }

    if (entity == null) {
      return null;
    }
    if (triple.getSubject().matches(s)) {
      return new Triple(entity, triple.getPredicate(), triple.getObject());
    } else if (triple.getObject().matches(s)) {
      return new Triple(triple.getSubject(), triple.getPredicate(), entity);
    } else {
      return null;
    }
  }

  private Element getStart(Element element, Element pattern) {
    if (element instanceof Entity || element instanceof Node) {
      return element.bind(pattern);
    } else if (((Triple) element).getSubject() instanceof Entity) {
      Element subject = ((Triple) pattern).getSubject();
      return ((Triple) element).getSubject().bind(subject);
    } else {
      Element object = ((Triple) pattern).getObject();
      return ((Triple) element).getObject().bind(object);
    }
  }

  private List<List<Result>> prepareElement(
      List<Result> evidences, Triple pattern, Map<String, Object> context) {
    if (CollectionUtils.isEmpty(evidences)) {
      return Arrays.asList(new LinkedList<>(prepareElement(pattern, context)));
    }
    List<List<Result>> rst = new LinkedList<>();
    Collection<Result> curRst = prepareElement(pattern, context);
    for (Result r : curRst) {
      List<Result> merged = new LinkedList<>(evidences);
      merged.add(r);
      if (reserve(merged)) {
        rst.add(merged);
      }
    }
    return rst;
  }

  private Boolean reserve(List<Result> evidence) {
    Map<String, String> aliasToId = new HashMap<>();
    for (Result r : evidence) {
      Element e = r.getData();
      if (e instanceof Entity) {
        if (!aliasToId.containsKey(e.alias())) {
          aliasToId.put(e.alias(), ((Entity) e).getId());
        }
        if (!StringUtils.equals(((Entity) e).getId(), aliasToId.get(e.alias()))) {
          return false;
        }
      } else {
        Entity s = (Entity) ((Triple) e).getSubject();
        Entity o = (Entity) ((Triple) e).getObject();
        if (!aliasToId.containsKey(s.alias())) {
          aliasToId.put(s.alias(), s.getId());
        }
        if (!aliasToId.containsKey(o.alias())) {
          aliasToId.put(o.alias(), o.getId());
        }
        if (!StringUtils.equals(s.getId(), aliasToId.get(s.alias()))) {
          return false;
        }
        if (!StringUtils.equals(o.getId(), aliasToId.get(o.alias()))) {
          return false;
        }
      }
    }
    return true;
  }

  private Collection<Result> prepareElement(Element pattern, Map<String, Object> context) {
    Collection<Result> result;
    Collection<Element> spo = this.tripleStore.find(pattern);
    if (spo == null || spo.isEmpty()) {
      if (pattern instanceof Node) {
        result = find((Node) pattern, context);
      } else {
        result = find((Triple) pattern, context);
      }
    } else {
      result = spo.stream().map(e -> new Result(e, null)).collect(Collectors.toList());
    }
    for (Result r : result) {
      r.setData(r.getData().bind(pattern));
    }
    return result;
  }

  public void addEntity(Entity entity) {
    this.tripleStore.addEntity(entity);
  }

  public void addTriple(Triple triple) {
    this.tripleStore.addTriple(triple);
  }

  public void clear() {
    this.tripleStore.clear();
  }
}
