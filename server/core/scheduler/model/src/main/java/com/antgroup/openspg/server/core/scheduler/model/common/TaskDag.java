/*
 * Copyright 2023 Ant Group CO., Ltd.
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
package com.antgroup.openspg.server.core.scheduler.model.common;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

/** Task Dag Model ,Contains nodes and edges */
@Getter
@Setter
@ToString
public class TaskDag {

  /** dag nodes List */
  private List<Node> nodes = Collections.emptyList();

  /** dag edges List */
  private List<Edge> edges = Collections.emptyList();

  /** dag extend */
  private String extend;

  /** get node by id list */
  @JSONField(serialize = false)
  public List<Node> getNode(List<String> idList) {
    List<Node> nodes = Lists.newArrayList();
    if (CollectionUtils.isEmpty(idList)) {
      return nodes;
    }
    for (Node node : this.nodes) {
      if (idList.contains(node.getId())) {
        nodes.add(node);
      }
    }
    return nodes;
  }

  /** get Next Nodes */
  @JSONField(serialize = false)
  public List<Node> getNextNodes(String nodeId) {
    List<String> idList = Lists.newArrayList();
    for (Edge edge : edges) {
      if (edge.getFrom().equals(nodeId)) {
        idList.add(edge.getTo());
      }
    }
    return getNode(idList);
  }

  /** get Pre Nodes */
  @JSONField(serialize = false)
  public List<Node> getPreNodes(String nodeId) {
    List<String> idList = Lists.newArrayList();
    for (Edge edge : edges) {
      if (edge.getTo().equals(nodeId)) {
        idList.add(edge.getFrom());
      }
    }
    return getNode(idList);
  }

  @Getter
  @Setter
  @ToString
  public static class Node {
    /** id */
    private String id;

    /** type */
    private String type;

    /** name */
    private String name;

    /** properties */
    private JSONObject properties;
  }

  @Getter
  @Setter
  @ToString
  public static class Edge {
    /** from id */
    private String from;

    /** to id */
    private String to;
  }
}
