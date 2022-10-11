import cv2
import numpy as np

def calculateThresholdValue(path):
    image = cv2.imread(path,0)


     image_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    if image_gray is None:
        return 0
    return 1

#      histg = cv2.calcHist([image_gray],[0],None,[255],[0,255])
#      within = []
#
#      for i in range(len(histg)):
#          x,y = np.split(histg,[i])
#          x1 = np.sum(x)/(image.shape[0]*image.shape[1])
#          y1 = np.sum(y)/(image.shape[0]*image.shape[1])
#
#          x2 = np.sum([j*t for j,t in enumerate(x)])/np.sum(x)
#          y2 = np.sum([j*t for j,t in enumerate(y)])/np.sum(y)
#
#          x3 = np.sum([(j-x2)**2*t for j,t in enumerate(x)])/np.sum(x)
#          x3 = np.nan_to_num(x3)
#          y3 = np.sum([(j-y2)**2*t for j,t in enumerate(y)])/np.sum(y)
#          within.append(x1*x3 + y1*y3)
#
#      m = np.argmin(within)
#      return m