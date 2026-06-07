from collections import deque

import numpy as np

from app.buffer.normalizer import Normalizer


class StreamBuffer:
    """
           Per-stream buffer with a two-phase lifecycle:

             1. COLLECTING - gather `training_size` raw readings, then fit & freeze a
                Normalizer on them.
             2. READY      - normalize each new reading and push it into a sliding
                window of `window_size`. Emit the window once it is full.

           add() returns None while warming up or while the window is not yet full,
           and returns the ready window (1-D np.ndarray of length window_size) once
           there are enough normalized readings to run inference.
    """

    def __init__(self, window_size: int, training_size: int, eps: float = 1e-8):
        self.window_size = window_size
        self.training_size = training_size
        self.normalizer = Normalizer(eps)
        self._training: list[float] = []  # war readings, pre-fit
        self._window: deque[float] = deque(maxlen=window_size)  # normalized

    @property
    def is_ready(self) -> bool:
        return self.normalizer.is_fitted

    def add(self, value: float) -> np.ndarray | None:
        if not self.is_ready:
            self._training.append(value)
            if len(self._training) >= self.training_size:
                self.normalizer.fit(self._training)
            return None

        normalized_value = self.normalizer.transform([value])[0]
        self._window.append(normalized_value)
        if len(self._window) < self.window_size:
            return None
        else:
            return np.array(self._window)
