import os

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9094")
KAFKA_CONSUMER_GROUP=os.getenv("KAFKA_CONSUMER_GROUP", "lstm-anomaly-group")
KAFKA_INPUT_TOPIC = os.getenv("KAFKA_INPUT_TOPIC", "data")
KAFKA_OUTPUT_TOPIC = os.getenv("KAFKA_OUTPUT_TOPIC", "anomaly-detected-ml")

# sliding window — how many readings to feed into the LSTM
WINDOW_SIZE = int(os.getenv("WINDOW_SIZE", "20"))

# minimum readings before we start inference
MIN_READINGS = int(os.getenv("MIN_READINGS", "30"))

# reconstruction error above this → anomaly
RECONSTRUCTION_THRESHOLD = float(os.getenv("RECONSTRUCTION_THRESHOLD", "0.5"))

# how many readings to train on before first inference
TRAINING_SIZE = int(os.getenv("TRAINING_SIZE", "50"))