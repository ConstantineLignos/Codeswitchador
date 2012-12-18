"""Unicode-safe CSV reader.

This file is an adaptation of content from the Python Tutorial and
Stack Overflow answers.

"""

import csv
import codecs
import cStringIO

class UTF8Recoder:
    """Iterator that reads an encoded stream and reencodes the input to UTF-8."""

    def __init__(self, f, encoding):
        self.reader = codecs.getreader(encoding)(f)

    def __iter__(self):
        return self

    def next(self):
        return self.reader.next().encode("utf-8")


class UnicodeCSVReader:
    """A CSV reader that supports Unicode files."""

    def __init__(self, f, dialect=csv.excel, encoding="utf-8", **kwds):
        f = UTF8Recoder(f, encoding)
        self.reader = csv.reader(f, dialect=dialect, **kwds)

    def next(self):
        row = self.reader.next()
        return [unicode(s, "utf-8") for s in row]

    def __iter__(self):
        return self

    @property
    def line_num(self):
        return self.reader.line_num


class UnicodeCSVWriter:
    """A CSV Writer that supports Unicode files."""

    def __init__(self, f, dialect=csv.excel, encoding="utf-8", **kwds):
        # Redirect output to a queue
        self.queue = cStringIO.StringIO()
        self.writer = csv.writer(self.queue, dialect=dialect, **kwds)
        self.stream = f
        self.encoder = codecs.getincrementalencoder(encoding)()

    def writerow(self, row):
        self.writer.writerow([s.encode("utf-8") for s in row])
        # Fetch UTF-8 output from the queue ...
        data = self.queue.getvalue()
        data = data.decode("utf-8")
        # ... and reencode it into the target encoding
        data = self.encoder.encode(data)
        # write to the target stream
        self.stream.write(data)
        # empty queue
        self.queue.truncate(0)

    def writerows(self, rows):
        for row in rows:
            self.writerow(row)


class UnicodeDictReader(csv.DictReader):
    def __init__(self, f, encoding="utf-8", fieldnames=None, **kwds):
        csv.DictReader.__init__(self, f, fieldnames=fieldnames, **kwds)
        self.reader = UnicodeCSVReader(f, encoding=encoding, **kwds)


class UnicodeDictWriter(csv.DictWriter):
    def __init__(self, f, fieldnames, encoding="utf-8", **kwds):
        csv.DictWriter.__init__(self, f, fieldnames, **kwds)
        self.writer = UnicodeCSVWriter(f, encoding=encoding, **kwds)
