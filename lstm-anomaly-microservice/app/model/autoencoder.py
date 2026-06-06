import torch
import torch.nn as nn


class LSTMAutoencoder(nn.Module):
    """
    LSTM Autoencoder for time series anomaly detection.

    Encoder compresses the input sequence into a fixed-size latent vector.
    Decoder reconstructs the sequence from that vector.
    High reconstruction error = anomaly.

    Tensors are (batch, seq_len, features) throughout (batch_first=True).
    """

    def __init__(self, input_size: int = 1, hidden_size: int = 32, num_layers: int = 1):
        super().__init__()

        self.hidden_size = hidden_size
        self.num_layers = num_layers

        self.encoder = nn.LSTM(
            input_size=input_size,
            hidden_size=hidden_size,
            num_layers=num_layers,
            batch_first=True,
        )

        self.decoder = nn.LSTM(
            input_size=hidden_size,
            hidden_size=hidden_size,
            num_layers=num_layers,
            batch_first=True,
        )

        self.output_layer = nn.Linear(in_features=hidden_size, out_features=input_size)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        batch_size, seq_len, _ = x.shape

        _, (h_n, _) = self.encoder(x)
        latent = h_n[-1]

        decoder_input = latent.unsqueeze(1).repeat(1, seq_len, 1)

        decoded_seq, _ = self.decoder(decoder_input)
        reconstruction = self.output_layer(decoded_seq)

        return reconstruction
