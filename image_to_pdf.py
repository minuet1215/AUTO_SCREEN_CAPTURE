import os
from PIL import Image
from io import BytesIO
from PyPDF4 import PdfFileMerger, PdfFileReader
from natsort import natsorted

image_files = natsorted([file for file in os.listdir('.') if file.endswith(
    '.jpg') or file.endswith('.png') or file.endswith('.gif')])

# Create a new PDF file
pdf_merger = PdfFileMerger()

# Iterate through the image files and add them to the PDF merger
for file in image_files:
    # Open the image and convert it to PDF format
    image = Image.open(file)
    pdf_image = image.convert('RGB')

    # Add the converted image to the PDF file
    pdf_page = BytesIO()
    pdf_image.save(pdf_page, format='PDF', quality=95)
    pdf_merger.append(PdfFileReader(pdf_page))

# Save the PDF file
with open('output.pdf', 'wb') as f:
    pdf_merger.write(f)
