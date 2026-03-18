-- Delete the im_data_import schema and all its tables, views, and functions.
-- The management of these tables has since moved to the Data Importer Service,
-- and the API codebase should no longer have any knowledge of the staging schema or its contents.
DROP SCHEMA IF EXISTS im_data_import CASCADE;