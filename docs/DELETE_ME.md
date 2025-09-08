# Pairing Session 2025-09-08

> [!WARNING]
> If this is commited to `main`, please delete me!! --TJWC 2025-09-08

## TODO

1. [Ryan] Remove the `region`, `office`, and `pdu` reference tables
    - tables are gone
    - tests all pass (that cause a failure of `2`)
2. Remove their associated Repositories
3. Update the `DeliveryLocationPreferences::offices` field (see below)
4. Add the `DeliveryLocationPreferencePdu` entity, and repository

```json
{
  "preferredDeliveryLocations": [
    {
      "officeCode": "OFFICE-1",
      "officeDescription": "Bristol Office",
      "dlpPduId": "123"
    }
  ]
}
```
