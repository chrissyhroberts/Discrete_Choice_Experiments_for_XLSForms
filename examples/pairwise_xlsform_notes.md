# XLSForm sketch for ODK Collect

Exact syntax may need adjustment depending on the ODK Collect external app widget version in use, but the intended pattern is:

```text
calculate | pairwise_options | Cost|Privacy|Speed|Offline use|Training burden
calculate | pairwise_rounds  | 5
calculate | pairwise_seed    | ${instanceID}
text      | pairwise_result  | appearance: ex:org.lshtm.choice.PAIRWISE(options=${pairwise_options},rounds=5,options_per_round=2,seed=${pairwise_seed},session_id=${instanceID})
```

For production forms, keep the options in a calculate field or generated value rather than hard-coding a long string into the appearance column.

Expected returned value is a JSON string stored in `pairwise_result`.
```
