module "s3bucket" {
  source = "./s3bucket"
  bucket_name = var.bucket_name
  region      = var.region
  environment   = var.environment
}

module "vpc" {
  source = "./vpc"
  cidr_block = var.cidr_block
  
}

module "subnet" {
  depends_on = [ module.vpc ]
  source = "./subnet"
  vpc_id = module.vpc.vpc_id
  cidr_block = var.subnet_cidr_block
  
}

# module "ec2" {
#   source = "./ec2"
#   bucket_name = module.s3bucket.bucket_name
#   vpc_id = module.vpc.vpc
#   subnet_id = module.subnet.subnet_id
 
# }
