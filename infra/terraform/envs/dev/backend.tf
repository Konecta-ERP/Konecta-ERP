terraform {
  backend "gcs" {
    bucket = "erp-konecta-477213-terraform-state"
    prefix = "dev/"
  }
}