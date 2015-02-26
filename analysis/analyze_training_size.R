library(ggplot2)
theme_set(theme_bw())

# Load in each TSV and add them together
perf <- read.csv("../perf/twothirds/perf.csv")
perf$Size <- perf$Name
perf$Name <- NULL
perf$Features <- factor(perf$Features, levels = c("Trigram", "Bigram", "Nochar", "Token", "TokenTag"))
summary(perf)

# Prune a little
perf <- subset(perf, !Features %in% c("Bigram", "Nochar"))

ggplot(perf, aes(Size, Accuracy)) + geom_hline(aes(yintercept = .972), linetype = "dashed") + geom_line(aes(color = Features), size = 1) +  geom_point() + theme(legend.position="bottom") + xlab("Training Tweets")
ggsave("perceptron_perf.pdf", height = 4, width = 6)


ggplot(perf, aes(Size, OOV)) + geom_line(size = 1) +  geom_point() + scale_y_continuous(limits = c(0, 1)) + ylab("OOV rate") + xlab("Training Tweets")
ggsave("oov_rate.pdf", height = 4, width = 6) 
