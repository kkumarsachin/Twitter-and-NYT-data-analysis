data<-read.csv(file.choose(),header = T)
data<-data[order(-data$Count),]
data<-data[1:20,]
k = barplot(data$Count,main="Twitter Word co-occurence",col=rainbow(20))
text(k, labels = data$word,par("usr")[3], srt = 30, adj = c(1,1), xpd = TRUE, cex=1)