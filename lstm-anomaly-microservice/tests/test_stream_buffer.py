import numpy as np

from app.buffer.stream_buffer import StreamBuffer


def test_collecting_phase_returns_none_and_not_ready():
    buf = StreamBuffer(window_size=3, training_size=5)

    for i in range(4):
        assert buf.add(10.0 + i) is None
        assert not buf.is_ready


def test_becomes_ready_after_training_size_readings():
    buf = StreamBuffer(window_size=3, training_size=5)

    out = None
    for i in range(5):
        out = buf.add(10.0 + i)

    assert buf.is_ready
    assert out is None  # fit happened, but the window is still empty


def test_emits_full_window_after_enough_post_training_readings():
    buf = StreamBuffer(window_size=3, training_size=5)
    for i in range(5):
        buf.add(10.0 + i)  # train

    assert buf.add(20.0) is None       # window: 1/3
    assert buf.add(21.0) is None       # window: 2/3
    window = buf.add(22.0)             # window: 3/3 -> emit

    assert isinstance(window, np.ndarray)
    assert window.shape == (3,)


def test_emitted_window_is_normalized():
    # Train on a spread of values; a reading equal to the training mean should
    # normalize to ~0, proving the window carries standardized data.
    train = np.arange(100.0, 150.0)  # 50 readings
    buf = StreamBuffer(window_size=2, training_size=len(train))
    for v in train:
        buf.add(v)

    buf.add(float(train.mean()))
    window = buf.add(float(train.mean()))

    assert np.allclose(window, 0.0, atol=1e-6)


def test_window_slides_and_keeps_latest():
    buf = StreamBuffer(window_size=3, training_size=5)
    for i in range(5):
        buf.add(10.0 + i)

    buf.add(1.0)
    buf.add(2.0)
    buf.add(3.0)
    last = buf.add(4.0)  # 1.0 has fallen off; window now holds 2, 3, 4

    assert last.shape == (3,)
    # newest reading sits at the end of the window
    assert last[-1] == buf.normalizer.transform([4.0])[0]


def test_anomaly_value_dominates_its_window():
    rng = np.random.default_rng(7)
    train = rng.normal(loc=20.0, scale=1.0, size=60)
    buf = StreamBuffer(window_size=5, training_size=len(train))
    for v in train:
        buf.add(v)

    for _ in range(4):
        buf.add(20.0)
    window = buf.add(95.0)  # spike enters as the newest reading

    assert abs(window[-1]) > 10.0
