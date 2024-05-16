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

package com.antgroup.openspg.reasoner.thinker.logic;

import com.antgroup.openspg.reasoner.thinker.logic.graph.Element;
import com.antgroup.openspg.reasoner.thinker.logic.rule.ClauseEntry;
import com.antgroup.openspg.reasoner.thinker.logic.rule.Rule;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class LogicNetwork {
  private Map<Element, Map<Element, List<Rule>>> forwardRules;
  private Map<Element, Map<List<Element>, List<Rule>>> backwardRules;

  public LogicNetwork() {
    this.forwardRules = new HashMap<>();
    this.backwardRules = new HashMap<>();
  }

  public void addRule(Rule rule) {
    for (ClauseEntry body : rule.getBody()) {
      Map<Element, List<Rule>> rules =
          forwardRules.computeIfAbsent(body.toElement(), (key) -> new HashMap<>());
      List<Rule> rList =
          rules.computeIfAbsent(rule.getHead().toElement(), (k) -> new LinkedList<>());
      rList.add(rule);
    }
    Map<List<Element>, List<Rule>> rules =
        backwardRules.computeIfAbsent(rule.getHead().toElement(), (key) -> new HashMap<>());
    List<Rule> rList =
        rules.computeIfAbsent(
            rule.getBody().stream().map(ClauseEntry::toElement).collect(Collectors.toList()),
            (k) -> new LinkedList<>());
    rList.add(rule);
  }

  public Collection<Rule> getForwardRules(Element e) {
    Set<Rule> rules = new HashSet<>();
    for (Map.Entry<Element, Map<Element, List<Rule>>> entry : forwardRules.entrySet()) {
      if (entry.getKey().matches(e)) {
        entry.getValue().values().stream().forEach(list -> rules.addAll(list));
      }
    }
    return rules;
  }

  public Collection<Rule> getBackwardRules(Element triple) {
    Set<Rule> rules = new HashSet<>();
    for (Map.Entry<Element, Map<List<Element>, List<Rule>>> entry : backwardRules.entrySet()) {
      if (entry.getKey().matches(triple)) {
        entry.getValue().values().stream().forEach(list -> rules.addAll(list));
      }
    }
    return rules;
  }
}
