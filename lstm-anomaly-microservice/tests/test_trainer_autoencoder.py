import numpy as np
import torch

from app.model.autoencoder import LSTMAutoencoder
from app.model.trainer_autoencoder import TrainerAutoencoder


def _normal_windows(n, w, seed=0):
    """A smooth, learnable pattern (half sine) plus tiny noise; shape (n, w)."""
    rng = np.random.default_rng(seed)
    base = np.sin(np.linspace(0.0, np.pi, w))
    return base[None, :] + rng.normal(0.0, 0.01, size=(n, w))


def _mse(model, window, w):
    x = torch.tensor(window, dtype=torch.float32).reshape(1, w, 1)
    with torch.no_grad():
        return torch.mean((model(x) - x) ** 2).item()


def test_returns_one_loss_per_epoch():
    torch.manual_seed(0)
    model = LSTMAutoencoder(hidden_size=16)
    history = TrainerAutoencoder(model, _normal_windows(8, 20), num_epochs=5, lr=1e-2).train()
    assert len(history) == 5


def test_loss_decreases():
    torch.manual_seed(0)
    model = LSTMAutoencoder(hidden_size=16)
    history = TrainerAutoencoder(model, _normal_windows(16, 20), num_epochs=40, lr=1e-2).train()
    assert history[-1] < history[0]


def test_accepts_unshaped_numpy_windows():
    # Trainer must add the feature axis itself: (N, W) -> (N, W, 1).
    torch.manual_seed(0)
    model = LSTMAutoencoder(hidden_size=16)
    trainer = TrainerAutoencoder(model, _normal_windows(4, 20), num_epochs=1)
    assert trainer.x.shape == (4, 20, 1)


def test_reconstructs_normal_better_than_anomaly():
    torch.manual_seed(0)
    w = 20
    normal = _normal_windows(32, w)
    model = LSTMAutoencoder(hidden_size=16)
    TrainerAutoencoder(model, normal, num_epochs=150, lr=1e-2).train()

    model.eval()
    normal_err = _mse(model, normal[0], w)
    anomaly = np.random.default_rng(99).normal(0.0, 1.0, size=w)  # unstructured noise
    anomaly_err = _mse(model, anomaly, w)

    assert anomaly_err > normal_err
