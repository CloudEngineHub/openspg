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

package com.antgroup.openspg.reasoner.thinker.logic.rule;

import com.antgroup.openspg.reasoner.thinker.logic.rule.visitor.RuleNodeVisitor;
import com.antgroup.openspg.reasoner.warehouse.common.VertexSubGraph;

import java.io.Serializable;
import java.util.Map;

public interface Node extends Serializable {
    <R> R accept(VertexSubGraph vertexGraph, Map<String, Object> context,  RuleNodeVisitor<R> visitor);
}
