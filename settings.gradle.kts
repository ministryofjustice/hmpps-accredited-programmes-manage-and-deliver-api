rootProject.name = "hmpps-accredited-programmes-manage-and-deliver-api"

buildCache {
  local {
    isEnabled = System.getenv("CI") == null
  }
}
