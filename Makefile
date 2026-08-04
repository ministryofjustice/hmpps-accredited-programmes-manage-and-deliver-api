.PHONY: local-up down clean logs ps seed teardown reseed restart-wiremock

# Number of referrals to seed.
SEED_COUNT ?= 50
# Number of groups to seed (referrals are allocated onto these).
GROUP_COUNT ?= 5

local-up:
	docker compose up -d --wait --wait-timeout 300
	@echo ""
	@echo "Local dependency stack ready:"
	@echo "  UI          http://localhost:3000"
	@echo "  AUTH        http://localhost:8090/auth"
	@echo "  POSTGRES    localhost:5432"
	@echo "  WIREMOCK    http://localhost:9095"
	@echo "  LOCALSTACK  http://localhost:4566"
	@echo ""
	@echo "Next: start the API from IntelliJ (profiles: dev,local,seeding),"
	@echo "then 'make seed' to generate referrals + groups."

down:
	docker compose down

clean:
	docker compose down -v

seed:
	./scripts/seed-data.sh seed $(SEED_COUNT) $(GROUP_COUNT)
	$(MAKE) restart-wiremock

# Remove all seeded data (groups then referrals).
teardown:
	./scripts/seed-data.sh teardown

# Clean slate: remove previous data, then seed a fresh set of referrals and groups.
reseed: teardown seed

restart-wiremock:
	docker compose restart wiremock
