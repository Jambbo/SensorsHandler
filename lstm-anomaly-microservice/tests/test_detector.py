import numpy as np
import torch

from app.model.autoencoder import LSTMAutoencoder
from app.buffer.stream_buffer import StreamBuffer
from app.model.thresholder import Thresholder
from app.model.detector import Detector


def _make_detector(seed=0):
    torch.manual_seed(seed)
    model = LSTMAutoencoder(hidden_size=16)
    buffer = StreamBuffer(window_size=20, training_size=50)
    return Detector(
        model, buffer, train_window_count=64,
        thresholder=Thresholder(), num_epochs=40, lr=1e-2,
    )


def _normal_stream(n, seed=0):
    """A smooth periodic signal + small noise — the 'normal' the model learns."""
    t = np.arange(n)
    return np.sin(2 * np.pi * t / 30) + np.random.default_rng(seed).normal(0, 0.05, size=n)


def test_returns_none_and_untrained_during_warmup():
    det = _make_detector()

    for v in _normal_stream(40):  # fewer than training_size -> normalizer not fit yet
        assert det.add(float(v)) is None

    assert not det._trained


def test_trains_and_calibrates_after_enough_windows():
    det = _make_detector()

    for v in _normal_stream(250):
        det.add(float(v))

    assert det._trained
    assert det.thresholder.threshold is not None


def test_no_verdict_until_trained():
    det = _make_detector()

    verdicts = [det.add(float(v)) for v in _normal_stream(250)]

    assert any(v is not None for v in verdicts)   # it did eventually start detecting
    assert all(v is None for v in verdicts[:64])  # nothing flagged before training


def test_flags_obvious_spike():
    det = _make_detector()
    for v in _normal_stream(250):
        det.add(float(v))
    assert det._trained

    assert det.add(50.0)  # a huge spike must be flagged as an anomaly
