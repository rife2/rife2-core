# JSONTestSuite corpus

These files are vendored from the JSONTestSuite conformance corpus:

* Source: https://github.com/nst/JSONTestSuite (`test_parsing` directory)
* Commit: `1ef36fa01286573e846ac449e8683f8833c5b26a` (November 22, 2024)
* License: MIT, see the `LICENSE` file in this directory

Two generated deep-nesting documents from the upstream corpus,
`n_structure_100000_opening_arrays.json` and
`n_structure_open_array_object.json`, aren't vendored to keep the resources
small, `rife.json.TestJsonConformance` recreates them at test time instead.
