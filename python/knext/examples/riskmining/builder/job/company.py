# -*- coding: utf-8 -*-
# Copyright 2023 Ant Group CO., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied.

from knext.client.model.builder_job import BuilderJob
from knext.api.component import SPGTypeMapping
from knext.api.component import (
    CsvSourceReader,
    KGSinkWriter,
)
from knext.component.builder import RelationMapping
from knext.examples.riskmining.schema.riskmining_schema_helper import RiskMining


class Company(BuilderJob):
    def build(self):
        source = CsvSourceReader(
            local_path="./builder/job/data/Company.csv",
            columns=["id", "name", "phone"],
            start_row=2,
        )

        mapping = (
            SPGTypeMapping(spg_type_name=RiskMining.Company.__typename__)
            .add_field("id", RiskMining.Company.id)
            .add_field("name", RiskMining.Company.name)
            .add_field("phone", RiskMining.Company.hasPhone)
        )

        sink = KGSinkWriter()

        return source >> mapping >> sink


class CompanyHasCert(BuilderJob):
    def build(self):
        source = CsvSourceReader(
            local_path="./builder/job/data/Company_hasCert_Cert.csv",
            columns=["src", "dst"],
            start_row=2,
        )

        mapping = (
            RelationMapping(
                subject_name=RiskMining.Company.__typename__,
                predicate_name="hasCert",
                object_name=RiskMining.Cert.__typename__,
            )
            .add_field("src", "srcId")
            .add_field("dst", "dstId")
        )

        sink = KGSinkWriter()

        return source >> mapping >> sink
