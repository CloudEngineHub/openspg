package com.antgroup.openspg.reasoner.thinker

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import com.antgroup.openspg.reasoner.thinker.SimplifyDSLParser._
import com.antgroup.openspg.reasoner.thinker.logic.graph.{Concept, Element}
import com.antgroup.openspg.reasoner.thinker.logic.rule.{Node, Rule}
import com.antgroup.openspg.reasoner.thinker.logic.rule.exact.Or

class SimplifyThinkerParser {
  var param: Map[String, Object] = Map.empty
  val thinkerRuleParser: ThinkerRuleParser = new ThinkerRuleParser()

  def parseSimplifyDsl(
      simplifyDSL: String,
      param: Map[String, Object] = Map.empty): List[Rule] = {
    val parser = new SimplifyThinkerLexerInit().initSimplifyThinkerParser(simplifyDSL)
    this.param = param
    parseScript(parser.script())
  }

  def parseScript(ctx: ScriptContext): List[Rule] = {
    val ruleResult: mutable.ListBuffer[Rule] = mutable.ListBuffer.empty
    if (ctx.define_rule_on_concept() != null && ctx.define_rule_on_concept().size() > 0) {
      ctx
        .define_rule_on_concept()
        .asScala
        .foreach(rule => {
          ruleResult += parseDefineRuleOnConcept(rule)
        })
    }
    if (ctx.define_rule_on_relation_to_concept() != null
      && ctx.define_rule_on_relation_to_concept().size() > 0) {
      ctx
        .define_rule_on_relation_to_concept()
        .asScala
        .foreach(rule => {
          ruleResult += parseDefineRuleOnRelationToConcept(rule)
        })
    }
    if (ctx.define_proiority_rule_on_concept() != null
      && ctx.define_proiority_rule_on_concept().size() > 0) {
      ctx
        .define_proiority_rule_on_concept()
        .asScala
        .foreach(rule => {
          ruleResult += parseDefinePriorityRuleOnConcept(rule)
        })
    }
    ruleResult.toList
  }

  def parseDefineRuleOnConcept(ctx: Define_rule_on_conceptContext): Rule = {
    val rule = new Rule()
    rule.setTriggerName(null)
    rule.setHead(
      new Concept(
        ctx
          .define_rule_on_concept_structure()
          .concept_declaration()
          .concept_name()
          .getText))

    val ruleAndAction: SimplifyDSLParser.Rule_and_action_bodyContext =
      ctx.define_rule_on_concept_structure().rule_and_action_body()
    if (ruleAndAction.rule_body_content() != null
      && ruleAndAction.rule_body_content().logical_statement() != null
      && ruleAndAction.rule_body_content().logical_statement().size() > 0) {
      val (root, body) = parseMultiLogicalStatement(
        ruleAndAction.rule_body_content().logical_statement().asScala.toList)
      rule.setRoot(root)
      rule.setBody(body.asJava)
    }
    rule
  }

  def parseMultiLogicalStatement(ctx: List[Logical_statementContext]): (Node, List[Element]) = {
    val body: ListBuffer[Element] = new mutable.ListBuffer[Element]()
    if (ctx.length > 1) {
      val orChildrenList: ListBuffer[Node] = new mutable.ListBuffer[Node]()
      ctx.foreach(logicalStatement => {
        orChildrenList += parseOneLogicalStatement(logicalStatement, body)
      })

      val or = new Or()
      or.setChildren(orChildrenList.toList.asJava)
      (or, body.distinct.toList)
    } else {
      (parseOneLogicalStatement(ctx.head, body), body.distinct.toList)
    }
  }

  def parseOneLogicalStatement(ctx: Logical_statementContext, body: ListBuffer[Element]): Node = {
    thinkerRuleParser.thinkerParseValueExpression(ctx.value_expression(), body)
  }

  def parseDefineRuleOnRelationToConcept(ctx: Define_rule_on_relation_to_conceptContext): Rule = {
    val rule = new Rule()
    rule
  }

  def parseDefinePriorityRuleOnConcept(ctx: Define_proiority_rule_on_conceptContext): Rule = {
    val rule = new Rule()
    rule
  }

}
