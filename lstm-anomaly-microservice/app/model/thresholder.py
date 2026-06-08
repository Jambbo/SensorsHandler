import numpy as np
import torch
from app.utils.to_tensor import to_tensor


class Thresholder:
    def __init__(self):
        self.threshold: float | None = None


    def fit(self, normal_errors):
        self.threshold = float(np.percentile(normal_errors,99))

    def reconstruction_error(self, model, windows) -> np.ndarray:
        model.eval()
        windows_tensor = to_tensor(windows)
        with torch.no_grad():
            output = model(windows_tensor)
            per_window = ((output-windows_tensor)**2).mean(dim=(1,2))
        return per_window.numpy()

    def is_anomaly(self, error)->bool:
        if self.threshold is None: raise RuntimeError("call fit() first")
        return error>self.threshold