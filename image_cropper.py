import os
from PIL import Image

for root, dirs, files in os.walk('./'):
    files = sorted(files)
    for idx, file in enumerate(files):
        fname, ext = os.path.splitext(file)
        if ext in ['.jpg', '.PNG', '.gif']:
            im = Image.open(file)
            width, height = im.size

            crop_image = im.crop((0, 180, width, height-180))
            # 좌, 우, 상, 하

            crop_image.save('Img' + str(idx) + '.png')
