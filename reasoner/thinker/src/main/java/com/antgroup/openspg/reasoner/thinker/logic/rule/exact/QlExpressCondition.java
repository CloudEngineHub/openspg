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

package com.antgroup.openspg.reasoner.thinker.logic.rule.exact;

import com.antgroup.openspg.reasoner.thinker.logic.graph.Element;
import com.antgroup.openspg.reasoner.thinker.logic.rule.TreeLogger;
import com.antgroup.openspg.reasoner.udf.rule.RuleRunner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

@Data
public class QlExpressCondition extends Condition {
  private static LoadingCache<String, Map<String, Set<String>>> varsCache;
  private String qlExpress;

  public QlExpressCondition(String qlExpress) {
    this.qlExpress = qlExpress;
  }

  static {
    synchronized (QlExpressCondition.class) {
      if (null == varsCache) {
        LoadingCache<String, Map<String, Set<String>>> tmpCache =
            CacheBuilder.newBuilder()
                .concurrencyLevel(8)
                .maximumSize(100)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(
                    new CacheLoader<String, Map<String, Set<String>>>() {
                      @Override
                      public Map<String, Set<String>> load(String rule) throws Exception {
                        try {
                          return RuleRunner.getInstance().getParamNames(rule);
                        } catch (Exception ex) {
                          throw new RuntimeException(ex);
                        }
                      }
                    });
        varsCache = tmpCache;
      }
    }
  }

  @Override
  public Boolean execute(List<Element> spoList, Map<String, Object> context, TreeLogger logger) {
    Map<String, Object> ruleCtx = new HashMap<>();
    if (context != null) {
      ruleCtx.putAll(context);
    }
    for (Element element : spoList) {
      ruleCtx.put(element.shortString(), true);
    }
    try {
      boolean absent = absent(ruleCtx);
      if (absent) {
        return null;
      }
      Object rst =
          RuleRunner.getInstance().executeExpression(ruleCtx, Arrays.asList(qlExpress), "");
      if (rst == null) {
        return false;
      } else {
        return (Boolean) rst;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private boolean absent(Map<String, Object> context) throws Exception {
    if (qlExpress.toLowerCase().contains("get_value")
        || qlExpress.toLowerCase().contains("get_spo")) {
      return false;
    }
    Map<String, Set<String>> vars = varsCache.get(qlExpress);
    for (String key : vars.keySet()) {
      if (CollectionUtils.isNotEmpty(vars.get(key))) {
        continue;
      }
      if (!context.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof QlExpressCondition)) {
      return false;
    }
    QlExpressCondition that = (QlExpressCondition) o;
    return Objects.equals(qlExpress, that.qlExpress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(qlExpress);
  }

  @Override
  public String toString() {
    return qlExpress;
  }
}
