import numpy as np
from app.model.trainer_autoencoder import TrainerAutoencoder


class Detector:
    def __init__(self, model, buffer, train_window_count, thresholder, num_epochs=30, lr=1e-3):
        self.model = model
        self.buffer = buffer
        self._trained = False
        self._collected: list[np.ndarray] = []
        self._train_window_count = train_window_count
        self.thresholder = thresholder
        self._num_epochs = num_epochs
        self._lr = lr

    def add(self, value) -> bool | None:
        window = self.buffer.add(value)

        # warming up phase, or window not full yet
        if window is None:
            return None
        # stockpile, then train once
        if not self._trained:
            self._collected.append(window)
            if len(self._collected) >= self._train_window_count:
                self._train_and_calibrate()
            return None
        # detect
        error = self.thresholder.reconstruction_error(self.model, window[None, :])[0]
        return self.thresholder.is_anomaly(error)

    def _train_and_calibrate(self):
        windows = np.array(self._collected)
        split = int(len(windows) * 0.8)
        train, test = windows[:split], windows[split:]
        TrainerAutoencoder(self.model, train, num_epochs=self._num_epochs, lr=self._lr).train()
        self.thresholder.fit(self.thresholder.reconstruction_error(self.model, test))
        self._trained = True
