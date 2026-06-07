import numpy as np
import pytest
import torch

from app.model.autoencoder import LSTMAutoencoder
from app.model.trainer_autoencoder import TrainerAutoencoder
from app.model.thresholder import Thresholder


# --- pure threshold logic (no model, fast & deterministic) ------------------

def test_fit_uses_99th_percentile():
    errors = np.linspace(0.0, 1.0, 1000)

    th = Thresholder()
    th.fit(errors)

    assert th.threshold == pytest.approx(float(np.percentile(errors, 99)))


def test_is_anomaly_compares_to_threshold():
    th = Thresholder()
    th.fit([0.1, 0.2, 0.3, 0.4])

    assert th.is_anomaly(th.threshold + 1.0)
    assert not th.is_anomaly(th.threshold - 1.0)


def test_is_anomaly_before_fit_raises():
    with pytest.raises(RuntimeError):
        Thresholder().is_anomaly(0.5)


def test_zero_threshold_is_treated_as_fitted():
    # A perfectly-reconstructed normal stream yields threshold == 0.0. That is a
    # VALID threshold (not "unfitted") -- is_anomaly must compare, not raise.
    th = Thresholder()
    th.fit([0.0, 0.0, 0.0])

    assert th.is_anomaly(0.5)  # must not raise


# --- scoring + end-to-end with a trained model ------------------------------

def test_reconstruction_error_returns_one_value_per_window():
    torch.manual_seed(0)
    model = LSTMAutoencoder(hidden_size=8)
    windows = np.random.default_rng(0).normal(0.0, 1.0, size=(5, 20))

    errors = Thresholder().reconstruction_error(model, windows)

    assert errors.shape == (5,)


def test_calibrated_threshold_separates_normal_from_spike():
    torch.manual_seed(0)
    w = 20
    base = np.sin(np.linspace(0.0, np.pi, w))
    train = base[None, :] + np.random.default_rng(1).normal(0.0, 0.02, size=(48, w))
    calib = base[None, :] + np.random.default_rng(2).normal(0.0, 0.02, size=(48, w))

    model = LSTMAutoencoder(hidden_size=16)
    TrainerAutoencoder(model, train, num_epochs=150, lr=1e-2).train()

    th = Thresholder()
    th.fit(th.reconstruction_error(model, calib))  # calibrate on held-out normal

    normal_err = th.reconstruction_error(model, base[None, :])[0]
    spike = base.copy()
    spike[10] = 8.0
    spike_err = th.reconstruction_error(model, spike[None, :])[0]

    assert not th.is_anomaly(normal_err)
    assert th.is_anomaly(spike_err)
