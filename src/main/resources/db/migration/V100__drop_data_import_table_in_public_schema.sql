-- The data import schema has been dropped but this table still exists and is causing issues in data importer service.
DROP TABLE IF EXISTS public.data_import_record;