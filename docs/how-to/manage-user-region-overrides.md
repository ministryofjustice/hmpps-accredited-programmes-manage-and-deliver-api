# Manage User Region Overrides

This guide explains how to grant or remove temporary region access for a user without waiting for team changes in nDelius.

## Summary

The API now reads effective user regions from:

1. nDelius team-derived regions
2. Database overrides in `user_region_override`

Overrides are intended for internal, short-lived rollout support.

## Table

Overrides are stored in:

- `user_region_override`

Active rows satisfy:

- `deleted_at IS NULL`

## Add a user region override

```sql
INSERT INTO user_region_override (username, region_name, created_by)
VALUES ('AUTH_ADM', 'South West', 'your.username');
```

## Remove a user region override (soft delete)

```sql
UPDATE user_region_override
SET deleted_at = CURRENT_TIMESTAMP,
    deleted_by = 'your.username'
WHERE lower(username) = lower('AUTH_ADM')
  AND region_name = 'South West'
  AND deleted_at IS NULL;
```

## View active overrides

```sql
SELECT id, username, region_name, created_at, created_by
FROM user_region_override
WHERE deleted_at IS NULL
ORDER BY lower(username), region_name;
```

## Notes

- Overrides are merged with nDelius regions, not replaced.
- If nDelius returns no regions for a user, active overrides still grant access for caselist and group-list region filtering.
- nDelius-derived region lookups use a 5 minute cache (`user-regions`), but override rows are read directly from the database on each request.