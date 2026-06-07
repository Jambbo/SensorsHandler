import numpy as np


class Normalizer:
    """
    Standardizes a single sensor stream: z = (x - mean) / std.

    Stats are fit ONCE on (assumed-normal) training data and then frozen, so
    later anomalies land far from the frozen mean instead of being absorbed
    into a per-window recalculation. One Normalizer instance per stream
    (i.e. per sensorId + measurementType).
    """

    def __init__(self, eps: float = 1e-8):
        self.eps = eps  # guards against divide-by-zero on a flat signal
        self.mean: float | None = None
        self.std: float | None = None

    @property
    def is_fitted(self) -> bool:
        return self.mean is not None

    # stats computed once and stored
    def fit(self, values) -> "Normalizer":
        values_arr = np.asarray(values, dtype=float)
        self.mean = values_arr.mean()
        self.std = values_arr.std()
        return self

    # standardization
    def transform(self, values):
        if not self.is_fitted: raise RuntimeError("call fit() first")
        return (np.asarray(values, dtype=float) - self.mean) / (self.std + self.eps)

    # map standardized vals back to the original scale
    def inverse_transform(self, values):
        return np.asarray(values, dtype=float)*(self.std + self.eps) + self.mean
