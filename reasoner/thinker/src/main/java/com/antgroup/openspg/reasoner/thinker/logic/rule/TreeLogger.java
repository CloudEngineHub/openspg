package com.antgroup.openspg.reasoner.thinker.logic.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeLogger implements Serializable {

  private static final long serialVersionUID = 8786588317154681976L;

  private static final String END_CON = "\u2514\u2500";
  private static final String CON = "\u251c\u2500";
  private static final String IDENT = "  ";
  private static final String V_IDENT = "\u2502 ";

  private String currentNodeName;
  private StringBuilder currentNodeMsg;
  private List<TreeLogger> children;

  public TreeLogger(String currentNodeName) {
    this.currentNodeName = currentNodeName;
  }

  public TreeLogger log(Object msg) {
    if (this.currentNodeMsg == null) {
      this.currentNodeMsg = new StringBuilder();
    }
    if (msg != null) {
      this.currentNodeMsg.append(msg);
    }
    return this;
  }

  public TreeLogger addChild(String name) {
    if (this.children == null) {
      this.children = new ArrayList<>();
    }
    TreeLogger ret = new TreeLogger(name);
    this.children.add(ret);
    return ret;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    genLog(sb, this, 0, false, new HashSet<>());
    return sb.toString();
  }

  private void genLog(
      StringBuilder sb,
      TreeLogger cur,
      int level,
      boolean hasNextSibling,
      Set<Integer> vertIdentIdxes) {
    // 生成本层内容的行
    if (level == 1) {
      sb.append(IDENT);
    } else if (level > 1) {
      for (int i = 0; i < level * 2 - 1; i++)
        if (vertIdentIdxes.contains(i)) {
          sb.append(V_IDENT);
        } else {
          sb.append(IDENT);
        }
    }
    if (level != 0) {
      if (hasNextSibling) {
        sb.append(CON);
      } else {
        sb.append(END_CON);
      }
    }

    sb.append(cur.currentNodeName);
    if (cur.currentNodeMsg != null) {
      sb.append(": ").append(cur.currentNodeMsg);
    }
    sb.append('\n');

    Set<Integer> childrenVerts = new HashSet<>(vertIdentIdxes);
    if (hasNextSibling) {
      childrenVerts.add(level * 2 - 1);
    }
    if (cur.children != null && !cur.children.isEmpty()) {
      for (int i = 0; i < cur.children.size(); i++) {
        TreeLogger c = cur.children.get(i);
        genLog(sb, c, level + 1, i != cur.children.size() - 1, childrenVerts);
      }
    }
  }

  /**
   * Setter method for property <tt>currentNodeName</tt>.
   *
   * @param currentNodeName value to be assigned to property currentNodeName
   */
  public void setCurrentNodeName(String currentNodeName) {
    this.currentNodeName = currentNodeName;
  }

  /**
   * Getter method for property <tt>currentNodeName</tt>.
   *
   * @return property value of currentNodeName
   */
  public String getCurrentNodeName() {
    return currentNodeName;
  }

  /**
   * Getter method for property <tt>currentNodeMsg</tt>.
   *
   * @return property value of currentNodeMsg
   */
  public StringBuilder getCurrentNodeMsg() {
    return currentNodeMsg;
  }

  /**
   * Setter method for property <tt>currentNodeMsg</tt>.
   *
   * @param currentNodeMsg value to be assigned to property currentNodeMsg
   */
  public void setCurrentNodeMsg(StringBuilder currentNodeMsg) {
    this.currentNodeMsg = currentNodeMsg;
  }

  /**
   * Getter method for property <tt>children</tt>.
   *
   * @return property value of children
   */
  public List<TreeLogger> getChildren() {
    return children;
  }

  /**
   * Setter method for property <tt>children</tt>.
   *
   * @param children value to be assigned to property children
   */
  public void setChildren(List<TreeLogger> children) {
    this.children = children;
  }
}
