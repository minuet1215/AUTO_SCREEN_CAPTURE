from os import walk, path
from PIL import Image

for _, _, files in walk('./'):
    files = sorted(files)
    for idx, file in enumerate(files):
        fname, ext = path.splitext(file)
        if ext in ['.jpg', '.PNG', '.gif']:
            im = Image.open(file)
            width, height = im.size

            crop_image = im.crop((0, 200, width, height-200))
            # 좌, 우, 상, 하

            crop_image.save('Img' + str(idx) + '.png')
