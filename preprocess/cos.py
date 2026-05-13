# -*- coding=utf-8
import logging
import os
from pathlib import Path
import sys

from qcloud_cos import CosConfig
from qcloud_cos import CosS3Client


logging.basicConfig(level=logging.INFO, stream=sys.stdout)


def main():
    secret_id = os.environ.get("QCLOUD_SECRET_ID")
    secret_key = os.environ.get("QCLOUD_SECRET_KEY")
    bucket = os.environ.get("QCLOUD_COS_BUCKET")
    region = os.environ.get("QCLOUD_COS_REGION", "ap-chengdu")
    token = os.environ.get("QCLOUD_COS_TOKEN")
    file_path = Path(os.environ.get("VECCARD_COS_FILE", "data/dingo.zip")).expanduser()
    object_key = os.environ.get("QCLOUD_COS_OBJECT", f"dingodb/{file_path.name}")

    if not secret_id or not secret_key or not bucket or not file_path.exists():
        return

    config = CosConfig(Region=region, SecretId=secret_id, SecretKey=secret_key, Token=token)
    client = CosS3Client(config)
    with file_path.open("rb") as fp:
        response = client.put_object(
            Bucket=bucket,
            Body=fp,
            Key=object_key,
            EnableMD5=True,
            StorageClass="STANDARD",
            ContentType="application/zip",
        )
        print(response["ETag"])


if __name__ == "__main__":
    main()
