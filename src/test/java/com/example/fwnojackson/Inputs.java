package com.example.fwnojackson;

public interface Inputs {
    final String PROJECT = "PROJECT";
    final String TASK = "TASK";
    final String JSON_INPUT = """
            {
            \t"items": [
            \t\t{
            \t\t\t"uid": "690ajgop520",
            \t\t\t"name": "luctus",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": null
            \t\t},
            \t\t{
            \t\t\t"uid": "740ppoir606",
            \t\t\t"name": "platea",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "690ajgop520"
            \t\t},
            \t\t{
            \t\t\t"uid": "543ukzwx920",
            \t\t\t"name": "id",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "740ppoir606"
            \t\t},
            \t\t{
            \t\t\t"uid": "270tmqyb719",
            \t\t\t"name": "fusce",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "543ukzwx920"
            \t\t},
            \t\t{
            \t\t\t"uid": "299xmmzn055",
            \t\t\t"name": "ultrices",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "270tmqyb719"
            \t\t},
            \t\t{
            \t\t\t"uid": "789trncv699",
            \t\t\t"name": "interdum",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-08-11",
            \t\t\t"endDate": "2023-01-09",
            \t\t\t"parentUid": "299xmmzn055"
            \t\t},
            \t\t{
            \t\t\t"uid": "963kiyoq591",
            \t\t\t"name": "dictumst",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "740ppoir606"
            \t\t},
            \t\t{
            \t\t\t"uid": "859izgaz739",
            \t\t\t"name": "vel",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "963kiyoq591"
            \t\t},
            \t\t{
            \t\t\t"uid": "202viesr740",
            \t\t\t"name": "luctus",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "859izgaz739"
            \t\t},
            \t\t{
            \t\t\t"uid": "359mgask657",
            \t\t\t"name": "porta",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-05-02",
            \t\t\t"endDate": "2020-10-27",
            \t\t\t"parentUid": "202viesr740"
            \t\t},
            \t\t{
            \t\t\t"uid": "577mcell206",
            \t\t\t"name": "at",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "859izgaz739"
            \t\t},
            \t\t{
            \t\t\t"uid": "708eypcf944",
            \t\t\t"name": "potenti",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-01-25",
            \t\t\t"endDate": "2022-07-30",
            \t\t\t"parentUid": "577mcell206"
            \t\t},
            \t\t{
            \t\t\t"uid": "156lqhrv998",
            \t\t\t"name": "curae",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "859izgaz739"
            \t\t},
            \t\t{
            \t\t\t"uid": "963tlift211",
            \t\t\t"name": "ipsum",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-03-29",
            \t\t\t"endDate": "2021-01-06",
            \t\t\t"parentUid": "156lqhrv998"
            \t\t},
            \t\t{
            \t\t\t"uid": "841aylde098",
            \t\t\t"name": "convallis",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "963kiyoq591"
            \t\t},
            \t\t{
            \t\t\t"uid": "227xjqbd522",
            \t\t\t"name": "blandit",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-12-10",
            \t\t\t"endDate": "2025-01-04",
            \t\t\t"parentUid": "841aylde098"
            \t\t},
            \t\t{
            \t\t\t"uid": "792hgugb955",
            \t\t\t"name": "pede",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "740ppoir606"
            \t\t},
            \t\t{
            \t\t\t"uid": "183dzcyg921",
            \t\t\t"name": "nonummy",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "792hgugb955"
            \t\t},
            \t\t{
            \t\t\t"uid": "259sooxv388",
            \t\t\t"name": "tellus",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "183dzcyg921"
            \t\t},
            \t\t{
            \t\t\t"uid": "156kxyep638",
            \t\t\t"name": "curabitur",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-03-11",
            \t\t\t"endDate": "2021-08-29",
            \t\t\t"parentUid": "259sooxv388"
            \t\t},
            \t\t{
            \t\t\t"uid": "291egdzr615",
            \t\t\t"name": "turpis",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-05-05",
            \t\t\t"endDate": "2021-05-22",
            \t\t\t"parentUid": "259sooxv388"
            \t\t},
            \t\t{
            \t\t\t"uid": "584pggko571",
            \t\t\t"name": "amet",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "183dzcyg921"
            \t\t},
            \t\t{
            \t\t\t"uid": "301nmutw647",
            \t\t\t"name": "curabitur",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-03-27",
            \t\t\t"endDate": "2024-07-15",
            \t\t\t"parentUid": "584pggko571"
            \t\t},
            \t\t{
            \t\t\t"uid": "743cngsr708",
            \t\t\t"name": "nisi",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "690ajgop520"
            \t\t},
            \t\t{
            \t\t\t"uid": "157feooe083",
            \t\t\t"name": "pede",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "743cngsr708"
            \t\t},
            \t\t{
            \t\t\t"uid": "306hhpee591",
            \t\t\t"name": "pellentesque",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "157feooe083"
            \t\t},
            \t\t{
            \t\t\t"uid": "954ynmnb449",
            \t\t\t"name": "lectus",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-09-15",
            \t\t\t"endDate": "2020-12-25",
            \t\t\t"parentUid": "306hhpee591"
            \t\t},
            \t\t{
            \t\t\t"uid": "939zdcnr344",
            \t\t\t"name": "felis",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "690ajgop520"
            \t\t},
            \t\t{
            \t\t\t"uid": "885gzard826",
            \t\t\t"name": "ornare",
            \t\t\t"type": "PROJECT",
            \t\t\t"startDate": null,
            \t\t\t"endDate": null,
            \t\t\t"parentUid": "939zdcnr344"
            \t\t},
            \t\t{
            \t\t\t"uid": "221wgjag761",
            \t\t\t"name": "vel",
            \t\t\t"type": "TASK",
            \t\t\t"startDate": "2020-11-24",
            \t\t\t"endDate": "2024-07-26",
            \t\t\t"parentUid": "885gzard826"
            \t\t}
            \t]
            }""";
}
