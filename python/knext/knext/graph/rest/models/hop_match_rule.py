# coding: utf-8
# Copyright 2023 OpenSPG Authors
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied.


"""
    knext

    No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)  # noqa: E501

    The version of the OpenAPI document: 1.0.0
    Generated by: https://openapi-generator.tech
"""


import pprint
import re  # noqa: F401

import six

from knext.common.rest.configuration import Configuration


class HopMatchRule(object):
    """NOTE: This class is auto generated by OpenAPI Generator.
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    """
    Attributes:
      openapi_types (dict): The key is attribute name
                            and the value is attribute type.
      attribute_map (dict): The key is attribute name
                            and the value is json key in definition.
    """
    openapi_types = {
        "edge_rule": "EdgeMatchRule",
        "adjacent_vertex_rule": "VertexMatchRule",
    }

    attribute_map = {
        "edge_rule": "edgeRule",
        "adjacent_vertex_rule": "adjacentVertexRule",
    }

    def __init__(
        self, edge_rule=None, adjacent_vertex_rule=None, local_vars_configuration=None
    ):  # noqa: E501
        """HopMatchRule - a model defined in OpenAPI"""  # noqa: E501
        if local_vars_configuration is None:
            local_vars_configuration = Configuration()
        self.local_vars_configuration = local_vars_configuration

        self._edge_rule = None
        self._adjacent_vertex_rule = None
        self.discriminator = None

        self.edge_rule = edge_rule
        if adjacent_vertex_rule is not None:
            self.adjacent_vertex_rule = adjacent_vertex_rule

    @property
    def edge_rule(self):
        """Gets the edge_rule of this HopMatchRule.  # noqa: E501


        :return: The edge_rule of this HopMatchRule.  # noqa: E501
        :rtype: EdgeMatchRule
        """
        return self._edge_rule

    @edge_rule.setter
    def edge_rule(self, edge_rule):
        """Sets the edge_rule of this HopMatchRule.


        :param edge_rule: The edge_rule of this HopMatchRule.  # noqa: E501
        :type: EdgeMatchRule
        """
        if (
            self.local_vars_configuration.client_side_validation and edge_rule is None
        ):  # noqa: E501
            raise ValueError(
                "Invalid value for `edge_rule`, must not be `None`"
            )  # noqa: E501

        self._edge_rule = edge_rule

    @property
    def adjacent_vertex_rule(self):
        """Gets the adjacent_vertex_rule of this HopMatchRule.  # noqa: E501


        :return: The adjacent_vertex_rule of this HopMatchRule.  # noqa: E501
        :rtype: VertexMatchRule
        """
        return self._adjacent_vertex_rule

    @adjacent_vertex_rule.setter
    def adjacent_vertex_rule(self, adjacent_vertex_rule):
        """Sets the adjacent_vertex_rule of this HopMatchRule.


        :param adjacent_vertex_rule: The adjacent_vertex_rule of this HopMatchRule.  # noqa: E501
        :type: VertexMatchRule
        """

        self._adjacent_vertex_rule = adjacent_vertex_rule

    def to_dict(self):
        """Returns the model properties as a dict"""
        result = {}

        for attr, _ in six.iteritems(self.openapi_types):
            value = getattr(self, attr)
            if isinstance(value, list):
                result[attr] = list(
                    map(lambda x: x.to_dict() if hasattr(x, "to_dict") else x, value)
                )
            elif hasattr(value, "to_dict"):
                result[attr] = value.to_dict()
            elif isinstance(value, dict):
                result[attr] = dict(
                    map(
                        lambda item: (item[0], item[1].to_dict())
                        if hasattr(item[1], "to_dict")
                        else item,
                        value.items(),
                    )
                )
            else:
                result[attr] = value

        return result

    def to_str(self):
        """Returns the string representation of the model"""
        return pprint.pformat(self.to_dict())

    def __repr__(self):
        """For `print` and `pprint`"""
        return self.to_str()

    def __eq__(self, other):
        """Returns true if both objects are equal"""
        if not isinstance(other, HopMatchRule):
            return False

        return self.to_dict() == other.to_dict()

    def __ne__(self, other):
        """Returns true if both objects are not equal"""
        if not isinstance(other, HopMatchRule):
            return True

        return self.to_dict() != other.to_dict()
