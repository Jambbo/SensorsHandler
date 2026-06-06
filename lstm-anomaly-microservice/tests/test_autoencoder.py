import torch

from app.model.autoencoder import LSTMAutoencoder


def test_reconstruction_matches_input_shape():
    model = LSTMAutoencoder(input_size=1, hidden_size=32, num_layers=1)
    x = torch.randn(4, 20, 1)

    out = model(x)

    assert out.shape == x.shape


def test_respects_custom_hidden_size():
    model = LSTMAutoencoder(input_size=1, hidden_size=64, num_layers=2)
    x = torch.randn(2, 15, 1)

    out = model(x)

    assert out.shape == x.shape
