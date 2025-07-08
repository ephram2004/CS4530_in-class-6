import redis
from redis.commands.json.path import Path
import time

r = redis.Redis(host="redis", port=6379, decode_responses=True)

# Retry loop for Redis availability and RedisJSON readiness
for attempt in range(10):
    try:
        r.ping()
        break
    except redis.exceptions.ConnectionError as e:
        print(f"Waiting for Redis... ({e})")
        time.sleep(2)
else:
    raise Exception("Redis never became connectable.")

# Optional: wipe everything (uncomment this if you want to always reset)
# r.flushall()
# print("Flushed all keys from Redis.")

# Wait for RedisJSON to be ready
for attempt in range(10):
    try:
        # Will error if RedisJSON isn't loaded
        r.json().set("property_data", "$", [])
        r.json().set("metrics", "$", [])
        print("Initialized Redis with separate root keys.")
        break
        break
    except redis.exceptions.ResponseError as e:
        print(f"Waiting for RedisJSON module... ({e})")
        time.sleep(2)
else:
    raise Exception("RedisJSON never became ready.")