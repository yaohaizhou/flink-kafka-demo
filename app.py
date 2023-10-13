from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
from torchvision.models import resnet18
import torch
import torchvision.transforms as transforms
from PIL import Image
import io
import requests

app = FastAPI()

# Load the pre-trained ResNet-18 model
model = resnet18(pretrained=True)
model.eval()

# Define the image preprocessing transform
preprocess = transforms.Compose([
    transforms.Resize(256),
    transforms.CenterCrop(224),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])

# Load class labels for ImageNet dataset (for ResNet-18)
LABELS_URL = 'https://raw.githubusercontent.com/anishathalye/imagenet-simple-labels/master/imagenet-simple-labels.json'
labels = requests.get(LABELS_URL).json()

@app.post("/classify/")
async def classify_image(file: UploadFile):
    try:
        # Read and preprocess the uploaded image
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes))
        input_tensor = preprocess(image)
        input_batch = input_tensor.unsqueeze(0)

        # Make predictions
        with torch.no_grad():
            output = model(input_batch)

        # Get the predicted class label
        _, predicted_idx = torch.max(output, 1)
        predicted_label = labels[predicted_idx.item()]
        print(predicted_label)

        return JSONResponse(content={"predicted_label": predicted_label}, status_code=200)
    except Exception as e:
        return JSONResponse(content={"error": str(e)}, status_code=500)
