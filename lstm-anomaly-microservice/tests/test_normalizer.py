import numpy as np
import pytest

from app.buffer.normalizer import Normalizer


def test_fit_learns_mean_and_std():
    data = np.array([10.0, 12.0, 14.0, 16.0, 18.0])

    norm = Normalizer().fit(data)  # fit must return self

    assert norm.is_fitted
    assert norm.mean == pytest.approx(14.0)
    assert norm.std == pytest.approx(np.std(data))


def test_transform_gives_zero_mean_unit_std():
    data = np.random.default_rng(0).normal(loc=230.0, scale=5.0, size=500)
    norm = Normalizer().fit(data)

    z = norm.transform(data)

    assert np.mean(z) == pytest.approx(0.0, abs=1e-6)
    assert np.std(z) == pytest.approx(1.0, abs=1e-6)


def test_transform_before_fit_raises():
    with pytest.raises(RuntimeError):
        Normalizer().transform([1.0, 2.0, 3.0])


def test_anomaly_survives_frozen_stats():
    # Fit on "normal" temperature around 20. A later spike to 95 must NOT be
    # absorbed -- it should map to a large standardized value (this is the
    # whole point of freezing the stats).
    normal = np.random.default_rng(1).normal(loc=20.0, scale=1.0, size=200)
    norm = Normalizer().fit(normal)

    z_spike = norm.transform([95.0])

    assert abs(z_spike[0]) > 10.0


def test_inverse_transform_round_trips():
    data = np.array([1.0, 5.0, 9.0, 13.0])
    norm = Normalizer().fit(data)

    restored = norm.inverse_transform(norm.transform(data))

    assert np.allclose(restored, data, atol=1e-4)


def test_constant_signal_does_not_explode():
    # A flat stream has std 0; eps must keep transform finite (no NaN/inf).
    norm = Normalizer().fit(np.full(50, 7.0))

    z = norm.transform([7.0])

    assert np.isfinite(z).all()
    assert z[0] == pytest.approx(0.0)
