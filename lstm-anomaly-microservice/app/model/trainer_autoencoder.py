import torch.nn as nn
import torch.optim as optim
from app.utils.to_tensor import to_tensor

class TrainerAutoencoder:
    def __init__(self, model, x, num_epochs=30, lr=1e-3):
        self.model = model
        self.x = to_tensor(x)
        self.num_epochs = num_epochs
        self.criterion = nn.MSELoss()
        self.optimizer = optim.Adam(self.model.parameters(), lr=lr)

    def train(self):
        history = []
        self.model.train()
        for epoch in range(self.num_epochs):
            self.optimizer.zero_grad()
            output = self.model(self.x)
            loss = self.criterion(output, self.x)
            loss.backward()
            self.optimizer.step()
            history.append(loss.item())
        return history

