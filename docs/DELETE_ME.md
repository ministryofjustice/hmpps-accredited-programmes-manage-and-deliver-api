# Pairing Session 2025-09-08

> [!WARNING]
> If this is commited to `main`, please delete me!! --TJWC 2025-09-08

## TODO

- [x] [Ryan] Remove the `region`, `office`, and `pdu` reference tables
    - [x] tables are gone
    - [x] tests all pass (that cause a failure of `2`)
- [x] Remove their associated Repositories

- [x] Update the `DeliveryLocationPreferences::offices` field (see below)
- [ ] Add the `DeliveryLocationPreferencePdu` entity, and repository

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
