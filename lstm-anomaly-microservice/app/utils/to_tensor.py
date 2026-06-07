import torch
def to_tensor(x)-> torch.Tensor:
    x = torch.as_tensor(x, dtype=torch.float32)
    if x.dim() == 2:
        x = x.unsqueeze(-1)
    return x