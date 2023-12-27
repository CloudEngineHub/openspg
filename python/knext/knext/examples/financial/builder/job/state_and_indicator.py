# -*- coding: utf-8 -*-

from knext.examples.financial.schema.financial_schema_helper import Financial

from knext.api.component import CSVReader, LLMBasedExtractor, KGWriter, SubGraphMapping
from knext.client.model.builder_job import BuilderJob
from nn4k.invoker import LLMInvoker


class StateAndIndicator(BuilderJob):
    def build(self):
        source = CSVReader(
            local_path="builder/job/data/document.csv", columns=["input"], start_row=2
        )

        from knext.examples.financial.builder.operator.IndicatorNER import IndicatorNER
        from knext.examples.financial.builder.operator.IndicatorREL import IndicatorREL
        from knext.examples.financial.builder.operator.IndicatorLOGIC import (
            IndicatorLOGIC,
        )

        extract = LLMBasedExtractor(
            llm=LLMInvoker.from_config("builder/model/openai_infer.json"),
            prompt_ops=[IndicatorNER(), IndicatorREL(), IndicatorLOGIC()],
        )

        state_mapping = (
            SubGraphMapping(spg_type_name=Financial.State)
            .add_mapping_field("id", Financial.State.id)
            .add_mapping_field("name", Financial.State.name)
            .add_mapping_field("causes", Financial.State.causes)
            .add_predicting_field(Financial.State.derivedFrom)
        )

        indicator_mapping = (
            SubGraphMapping(spg_type_name=Financial.Indicator)
            .add_mapping_field("id", Financial.Indicator.id)
            .add_mapping_field("name", Financial.Indicator.name)
        )

        sink = KGWriter()

        return source >> extract >> [state_mapping, indicator_mapping] >> sink


if __name__ == '__main__':
    from knext.api.auto_prompt import REPrompt
    prompt = REPrompt(
        spg_type_name=Financial.Company,
        property_names=[
            Financial.Company.orgCertNo,
            Financial.Company.regArea,
            Financial.Company.businessScope,
            Financial.Company.establishDate,
            Financial.Company.legalPerson,
            Financial.Company.regCapital
        ],
    )
    print(prompt.template)
