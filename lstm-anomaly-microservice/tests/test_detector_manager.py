from app.model.detector_manager import DetectorManager


class StubDetector:
    """Records every value it sees and returns a scripted verdict."""
    def __init__(self):
        self.values = []
        self.verdict = None  # what add() returns

    def add(self, value):
        self.values.append(value)
        return self.verdict


def _counting_factory():
    """Factory that hands out fresh StubDetectors and counts how many it made."""
    created = []
    def make():
        d = StubDetector()
        created.append(d)
        return d
    make.created = created  # introspect after the fact
    return make


def test_creates_one_detector_per_unique_key():
    factory = _counting_factory()
    mgr = DetectorManager(factory)

    mgr.add("s1", "temperature", 1.0)
    mgr.add("s2", "voltage", 2.0)
    mgr.add("s3", "power", 3.0)

    assert len(factory.created) == 3  # three distinct streams -> three detectors


def test_reuses_detector_for_same_key():
    factory = _counting_factory()
    mgr = DetectorManager(factory)

    mgr.add("s1", "temperature", 1.0)
    mgr.add("s1", "temperature", 2.0)
    mgr.add("s1", "temperature", 3.0)

    assert len(factory.created) == 1            # only ONE detector ever made
    assert factory.created[0].values == [1.0, 2.0, 3.0]  # all values went to it


def test_routes_values_to_the_correct_detector():
    factory = _counting_factory()
    mgr = DetectorManager(factory)

    mgr.add("s1", "temperature", 10.0)
    mgr.add("s2", "temperature", 20.0)  # same type, different sensor -> different stream
    mgr.add("s1", "temperature", 11.0)

    d_s1, d_s2 = factory.created
    assert d_s1.values == [10.0, 11.0]
    assert d_s2.values == [20.0]


def test_same_sensor_different_type_are_separate_streams():
    factory = _counting_factory()
    mgr = DetectorManager(factory)

    mgr.add("s1", "temperature", 1.0)
    mgr.add("s1", "voltage", 2.0)  # same sensor, different measurement -> separate

    assert len(factory.created) == 2


def test_passes_through_the_detector_verdict():
    factory = _counting_factory()
    mgr = DetectorManager(factory)

    mgr.add("s1", "temperature", 1.0)          # creates the detector
    factory.created[0].verdict = True          # script it to flag an anomaly

    assert mgr.add("s1", "temperature", 99.0) is True
