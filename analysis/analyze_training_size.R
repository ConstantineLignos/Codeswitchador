library(ggplot2)
theme_set(theme_bw())

# Load in each CSV and add them together
perf1 <- read.csv("../perf/twothirds/perf_perceptron.csv")
perf2 <- read.csv("../perf/twothirds/perf_hmm.csv")
perf <- rbind(perf1, perf2)

# Clean them up
perf$Size <- perf$Name
perf$Name <- NULL
perf$Features <- factor(perf$Features, levels = c("All", "Nochar", "Nolength", "Nosuffix", "Notagcontext", "Notokencontext", "HMMBigram"), labels = c("All", "No character", "No length", "No suffix", "No tag context", "No token context", "Bigram HMM"))      
summary(perf)

# Show just HMM
ggplot(subset(perf, Features == "Bigram HMM"), aes(Size, Accuracy)) + geom_hline(aes(yintercept = .972), linetype = "dashed") + geom_hline(aes(yintercept = .944), linetype = "dashed") + theme(legend.position="bottom") + xlab("Training Tweets") + scale_y_continuous(limits = c(0.94, 1.0), breaks = seq(.94, 1.0, 0.01)) + geom_line(aes(color = Features), size = 1) + geom_point() + scale_color_brewer(type = "qual", palette = 2)  + scale_color_discrete(name = "Model", labels = c("Bigram HMM")) + theme(legend.position="bottom")
ggsave("perf_hmm.pdf", height = 4, width = 6)

# Add "All"
ggplot(subset(perf, Features %in% c("All", "Bigram HMM")), aes(Size, Accuracy)) + geom_hline(aes(yintercept = .972), linetype = "dashed") + geom_hline(aes(yintercept = .944), linetype = "dashed") + xlab("Training Tweets") + scale_y_continuous(limits = c(0.94, 1.0), breaks = seq(.94, 1.0, 0.01)) + geom_line(aes(color = Features), size = 1) + geom_point() + scale_color_brewer(type = "qual", palette = 2) + scale_color_discrete(name = "Model", labels = c("Perceptron (all features)", "Bigram HMM")) + theme(legend.position="bottom")
ggsave("perf_hmm_perceptron.pdf", height = 4, width = 6)

# Show everything
ggplot(subset(perf, Features != "Bigram HMM"), aes(Size, Accuracy)) + geom_hline(aes(yintercept = .972), linetype = "dashed") + geom_hline(aes(yintercept = .944), linetype = "dashed") + xlab("Training Tweets") + scale_y_continuous(limits = c(0.94, 1.0), breaks = seq(.94, 1.0, 0.01)) + geom_line(aes(color = Features), size = 1) + geom_point() + scale_color_brewer(type = "qual", palette = 2)
ggsave("perf_perceptron.pdf", height = 4, width = 6)

# OOV
ggplot(subset(perf, Features %in% c("All", "Bigram HMM")), aes(Size, OOV)) + geom_line(aes(color = Features), size = 1) +  geom_point() + scale_y_continuous(limits = c(0, 1)) + ylab("OOV rate") + xlab("Training Tweets") + scale_color_brewer(type = "qual", palette = 2) + scale_color_discrete(name = "Model", labels = c("Perceptron", "Bigram HMM")) + theme(legend.position="bottom")
ggsave("oov_rate.pdf", height = 4, width = 6)

# Show just HMM
ggplot(subset(perf, Features == "Bigram HMM"), aes(Size, Accuracy)) + geom_hline(aes(yintercept = .972), linetype = "dashed") + geom_hline(aes(yintercept = .944), linetype = "dashed") + theme(legend.position="bottom") + xlab("Training Tweets") + scale_y_continuous(limits = c(0.94, 1.0), breaks = seq(.94, 1.0, 0.01)) + geom_line(aes(color = Features), size = 1) + geom_point() + scale_color_brewer(type = "qual", palette = 2)  + scale_color_discrete(name = "Model", labels = c("Bigram HMM")) + theme(legend.position="bottom")
ggsave("perf_hmm.pdf", height = 4, width = 6)

# Add "All"
ggplot(subset(perf, Features %in% c("All", "Bigram HMM")), aes(Size, Accuracy)) + geom_hline(aes(yintercept = .972), linetype = "dashed") + geom_hline(aes(yintercept = .944), linetype = "dashed") + xlab("Training Tweets") + scale_y_continuous(limits = c(0.94, 1.0), breaks = seq(.94, 1.0, 0.01)) + geom_line(aes(color = Features), size = 1) + geom_point() + scale_color_brewer(type = "qual", palette = 2) + scale_color_discrete(name = "Model", labels = c("Perceptron (all features)", "Bigram HMM")) + theme(legend.position="bottom")
ggsave("perf_hmm_perceptron.pdf", height = 4, width = 6)
