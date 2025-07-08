FROM python:3.11-slim

RUN pip install redis

WORKDIR /app
COPY init_data.py .

CMD ["python", "init_data.py"]