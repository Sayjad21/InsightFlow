import { jsPDF } from "jspdf";

type StyledText = { text: string; style: "normal" | "bold" | "italic" };

export class PdfExporter {
  pdf: jsPDF;
  yPosition = 20;
  lineHeight = 7;
  pageHeight: number;
  pageWidth: number;

  constructor() {
    this.pdf = new jsPDF();
    this.pageHeight = this.pdf.internal.pageSize.height - 20;
    this.pageWidth = this.pdf.internal.pageSize.width;
  }

  // Check for page break
  checkPageBreak(requiredHeight: number) {
    if (this.yPosition + requiredHeight > this.pageHeight) {
      this.pdf.addPage();
      this.yPosition = 20;
    }
  }

  async addImagesInPairs(
    images: Array<{ url: string; title: string }>,
    height: number = 70,
    width: number = 90
  ) {
    for (let i = 0; i < images.length; i += 2) {
      const imageHeight = height;
      const imageWidth = width;
      const gap = 5; // gap between images
      const totalWidth = imageWidth * 2 + gap;

      // Calculate left margin to center images horizontally
      const startX = (this.pageWidth - totalWidth) / 2;

      this.checkPageBreak(imageHeight + 30);

      const image1 = images[i];
      const image2 = images[i + 1];

      if (image1) {
        // Titles
        this.pdf.setFontSize(11);
        this.pdf.setFont("times", "bold");
        this.pdf.text(image1.title, startX, this.yPosition);
        if (image2)
          this.pdf.text(
            image2.title,
            startX + imageWidth + gap,
            this.yPosition
          );

        this.yPosition += 12;

        // First image
        const base64Data1 = await this.urlToBase64(image1.url);
        if (base64Data1) {
          this.pdf.addImage(
            base64Data1,
            "PNG",
            startX,
            this.yPosition,
            imageWidth,
            imageHeight
          );
        } else {
          this.pdf.setFontSize(10);
          this.pdf.setFont("times", "normal");
          this.pdf.text(
            `[Image: ${image1.title} - Unable to load]`,
            startX,
            this.yPosition + 20
          );
        }

        // Second image if exists
        if (image2) {
          const base64Data2 = await this.urlToBase64(image2.url);
          if (base64Data2) {
            this.pdf.addImage(
              base64Data2,
              "PNG",
              startX + imageWidth + gap,
              this.yPosition,
              imageWidth,
              imageHeight
            );
          } else {
            this.pdf.setFontSize(10);
            this.pdf.setFont("times", "normal");
            this.pdf.text(
              `[Image: ${image2.title} - Unable to load]`,
              startX + imageWidth + gap,
              this.yPosition + 20
            );
          }
        }

        this.yPosition += imageHeight + 15;
      }
    }
  }

  // Add text
  addText(
    text: string,
    fontSize = 10,
    fontStyle: "normal" | "bold" | "italic" = "normal"
  ) {
    this.checkPageBreak(this.lineHeight * 2);
    this.pdf.setFontSize(fontSize);
    this.pdf.setFont("times", fontStyle);

    const wrappedLines = this.pdf.splitTextToSize(text, 170);
    for (const line of wrappedLines) {
      this.checkPageBreak(this.lineHeight);
      this.pdf.text(line, 20, this.yPosition);
      this.yPosition += this.lineHeight;
    }
    this.yPosition += 3; // extra spacing
  }

  // Convert image URL to Base64
  async urlToBase64(url: string): Promise<string | null> {
    try {
      if (url.startsWith("data:")) return url;

      const response = await fetch(url, { mode: "cors", credentials: "omit" });
      if (!response.ok) return null;

      const blob = await response.blob();
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result as string);
        reader.onerror = reject;
        reader.readAsDataURL(blob);
      });
    } catch (err) {
      console.error("Error converting URL to base64:", err);
      return null;
    }
  }

  // Add image with title
  async addImage(
    imageDataOrUrl: string,
    title: string,
    maxWidth: number = 150,
    maxHeight: number = 100
  ) {
    try {
      const imageHeight = Math.min(maxHeight, 80);
      this.checkPageBreak(imageHeight + 20);

      // Title
      this.pdf.setFontSize(12);
      this.pdf.setFont("times", "bold");
      this.pdf.text(title, 20, this.yPosition);
      this.yPosition += 15;

      const imageX = (this.pageWidth - maxWidth) / 2;

      let base64 = imageDataOrUrl;
      if (imageDataOrUrl.startsWith("http")) {
        const loaded = await this.urlToBase64(imageDataOrUrl);
        if (!loaded) {
          this.addText(`[Image: ${title} - Unable to load]`, 10);
          return;
        }
        base64 = loaded;
      } else if (!imageDataOrUrl.startsWith("data:")) {
        base64 = `data:image/png;base64,${imageDataOrUrl}`;
      }

      this.pdf.addImage(
        base64,
        "PNG",
        imageX,
        this.yPosition,
        maxWidth,
        imageHeight
      );
      this.yPosition += imageHeight + 15;
    } catch (err) {
      console.error(`Failed to add image ${title}:`, err);
      this.addText(`[Image: ${title} - Error loading]`, 10);
    }
  }

  addMarkdown(text: string, fontSize = 10) {
    const regex = /(\*\*([^\*]+)\*\*|\*([^\*]+)\*)/g;

    // Split text by existing newlines to preserve structure
    const lines = text.split("\n");

    for (let line of lines) {
      if (!line.trim()) {
        this.yPosition += this.lineHeight; // preserve blank lines
        continue;
      }

      let currentX = 20;
      let lineFontSize = fontSize;
      let lineFontStyle: "normal" | "bold" | "italic" = "normal";

      // Handle heading ### -> bold and slightly larger
      if (
        line.startsWith("### ") ||
        line.startsWith("## ") ||
        line.startsWith("# ")
      ) {
        line = line.slice(4).trim();
        lineFontStyle = "bold";
        lineFontSize = fontSize + 2;
      }

      // Handle leading bullet
      if (line.startsWith("* ")) {
        this.checkPageBreak(this.lineHeight);
        this.pdf.setFont("times", "normal");
        this.pdf.text("•", currentX, this.yPosition); // bullet symbol
        currentX += this.pdf.getTextWidth("• ") + 2;
        line = line.slice(2);
      }

      const segments: StyledText[] = [];
      let lastIndex = 0;
      let match;
      while ((match = regex.exec(line)) !== null) {
        if (match.index > lastIndex) {
          segments.push({
            text: line.slice(lastIndex, match.index),
            style: "normal",
          });
        }
        if (match[2]) segments.push({ text: match[2], style: "bold" }); // **bold**
        if (match[3]) segments.push({ text: match[3], style: "italic" }); // *italic*
        lastIndex = regex.lastIndex;
      }
      if (lastIndex < line.length) {
        segments.push({ text: line.slice(lastIndex), style: "normal" });
      }

      // Render each segment inline with word wrapping
      for (const seg of segments) {
        this.pdf.setFont(
          "times",
          seg.style === "normal" ? lineFontStyle : seg.style
        );
        this.pdf.setFontSize(seg.style === "normal" ? lineFontSize : fontSize);

        const words = seg.text.split(/(\s+)/);
        for (const word of words) {
          const wordWidth = this.pdf.getTextWidth(word);

          // Check vertical page break
          if (this.yPosition + this.lineHeight > this.pageHeight) {
            this.pdf.addPage();
            this.yPosition = 20;
            currentX = 20;
          }

          // Horizontal wrap
          if (currentX + wordWidth > this.pageWidth - 20) {
            this.yPosition += this.lineHeight;
            currentX = 20;
            this.checkPageBreak(this.lineHeight);
          }

          this.pdf.text(word, currentX, this.yPosition);
          currentX += wordWidth;
        }
      }

      this.yPosition += this.lineHeight;
    }
  }

  // Save PDF
  save(fileName: string) {
    this.pdf.save(fileName);
  }
}
