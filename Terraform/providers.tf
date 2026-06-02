provider "aws" {
  region = "us-east-1"
}

terraform {
  backend "s3" {
    bucket = "my-terraform-s3-bucket-socgen-priya"
    key    = "devjune2026.tfstate"
    region = "us-east-1"
    
  }
}