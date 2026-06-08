class DetectorManager:
    def __init__(self, detector_factory):
        self._detectors = {}
        self.detector_factory = detector_factory

    def add(self, sensor_id, measurement_type, value) -> bool | None:
        key = (sensor_id, measurement_type)
        if key not in self._detectors:
            detector = self.detector_factory()
            self._detectors[key] = detector
        else:
            detector = self._detectors[key]
        return detector.add(value)
