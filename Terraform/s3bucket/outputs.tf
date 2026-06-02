output "s3bucket_name" {
  value = aws_s3_bucket.s3bucket.bucket
}

output "s3bucket_id" {
  value = aws_s3_bucket.s3bucket.id
}

output "s3bucket_arn" {
  value = aws_s3_bucket.s3bucket.arn
}
