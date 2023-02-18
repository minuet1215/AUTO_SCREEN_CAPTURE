from os import listdir
from PIL import Image
from io import BytesIO
from PyPDF4 import PdfFileMerger, PdfFileReader
from natsort import natsorted

image_files = natsorted([file for file in listdir('.') if file.endswith(
    '.jpg') or file.endswith('.png') or file.endswith('.gif')])

pdf_merger = PdfFileMerger()

for file in image_files:
    image = Image.open(file)
    pdf_image = image.convert('RGB')

    pdf_page = BytesIO()
    pdf_image.save(pdf_page, format='PDF', quality=95)
    pdf_merger.append(PdfFileReader(pdf_page))

with open('output.pdf', 'wb') as f:
    pdf_merger.write(f)
