#
# Autogenerated by Thrift Compiler (0.9.2)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#
#  options string: py
#

from thrift.Thrift import TType, TMessageType, TException, TApplicationException

from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol, TProtocol
try:
  from thrift.protocol import fastbinary
except:
  fastbinary = None



class FuncStat:
  """
  Attributes:
   - file
   - line
   - func_name
   - calls_count
   - total_time
   - own_time
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRING, 'file', None, None, ), # 1
    (2, TType.I32, 'line', None, None, ), # 2
    (3, TType.STRING, 'func_name', None, None, ), # 3
    (4, TType.I32, 'calls_count', None, None, ), # 4
    (5, TType.DOUBLE, 'total_time', None, None, ), # 5
    (6, TType.DOUBLE, 'own_time', None, None, ), # 6
  )

  def __init__(self, file=None, line=None, func_name=None, calls_count=None, total_time=None, own_time=None,):
    self.file = file
    self.line = line
    self.func_name = func_name
    self.calls_count = calls_count
    self.total_time = total_time
    self.own_time = own_time

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRING:
          self.file = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.I32:
          self.line = iprot.readI32();
        else:
          iprot.skip(ftype)
      elif fid == 3:
        if ftype == TType.STRING:
          self.func_name = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 4:
        if ftype == TType.I32:
          self.calls_count = iprot.readI32();
        else:
          iprot.skip(ftype)
      elif fid == 5:
        if ftype == TType.DOUBLE:
          self.total_time = iprot.readDouble();
        else:
          iprot.skip(ftype)
      elif fid == 6:
        if ftype == TType.DOUBLE:
          self.own_time = iprot.readDouble();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('FuncStat')
    if self.file is not None:
      oprot.writeFieldBegin('file', TType.STRING, 1)
      oprot.writeString(self.file)
      oprot.writeFieldEnd()
    if self.line is not None:
      oprot.writeFieldBegin('line', TType.I32, 2)
      oprot.writeI32(self.line)
      oprot.writeFieldEnd()
    if self.func_name is not None:
      oprot.writeFieldBegin('func_name', TType.STRING, 3)
      oprot.writeString(self.func_name)
      oprot.writeFieldEnd()
    if self.calls_count is not None:
      oprot.writeFieldBegin('calls_count', TType.I32, 4)
      oprot.writeI32(self.calls_count)
      oprot.writeFieldEnd()
    if self.total_time is not None:
      oprot.writeFieldBegin('total_time', TType.DOUBLE, 5)
      oprot.writeDouble(self.total_time)
      oprot.writeFieldEnd()
    if self.own_time is not None:
      oprot.writeFieldBegin('own_time', TType.DOUBLE, 6)
      oprot.writeDouble(self.own_time)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.file is None:
      raise TProtocol.TProtocolException(message='Required field file is unset!')
    if self.func_name is None:
      raise TProtocol.TProtocolException(message='Required field func_name is unset!')
    if self.calls_count is None:
      raise TProtocol.TProtocolException(message='Required field calls_count is unset!')
    if self.total_time is None:
      raise TProtocol.TProtocolException(message='Required field total_time is unset!')
    if self.own_time is None:
      raise TProtocol.TProtocolException(message='Required field own_time is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.file)
    value = (value * 31) ^ hash(self.line)
    value = (value * 31) ^ hash(self.func_name)
    value = (value * 31) ^ hash(self.calls_count)
    value = (value * 31) ^ hash(self.total_time)
    value = (value * 31) ^ hash(self.own_time)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class Function:
  """
  Attributes:
   - func_stat
   - callers
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRUCT, 'func_stat', (FuncStat, FuncStat.thrift_spec), None, ), # 1
    (2, TType.LIST, 'callers', (TType.STRUCT,(FuncStat, FuncStat.thrift_spec)), None, ), # 2
  )

  def __init__(self, func_stat=None, callers=None,):
    self.func_stat = func_stat
    self.callers = callers

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRUCT:
          self.func_stat = FuncStat()
          self.func_stat.read(iprot)
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.LIST:
          self.callers = []
          (_etype3, _size0) = iprot.readListBegin()
          for _i4 in xrange(_size0):
            _elem5 = FuncStat()
            _elem5.read(iprot)
            self.callers.append(_elem5)
          iprot.readListEnd()
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('Function')
    if self.func_stat is not None:
      oprot.writeFieldBegin('func_stat', TType.STRUCT, 1)
      self.func_stat.write(oprot)
      oprot.writeFieldEnd()
    if self.callers is not None:
      oprot.writeFieldBegin('callers', TType.LIST, 2)
      oprot.writeListBegin(TType.STRUCT, len(self.callers))
      for iter6 in self.callers:
        iter6.write(oprot)
      oprot.writeListEnd()
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.func_stat is None:
      raise TProtocol.TProtocolException(message='Required field func_stat is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.func_stat)
    value = (value * 31) ^ hash(self.callers)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class Stats:
  """
  Attributes:
   - func_stats
  """

  thrift_spec = (
    None, # 0
    (1, TType.LIST, 'func_stats', (TType.STRUCT,(Function, Function.thrift_spec)), None, ), # 1
  )

  def __init__(self, func_stats=None,):
    self.func_stats = func_stats

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.LIST:
          self.func_stats = []
          (_etype10, _size7) = iprot.readListBegin()
          for _i11 in xrange(_size7):
            _elem12 = Function()
            _elem12.read(iprot)
            self.func_stats.append(_elem12)
          iprot.readListEnd()
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('Stats')
    if self.func_stats is not None:
      oprot.writeFieldBegin('func_stats', TType.LIST, 1)
      oprot.writeListBegin(TType.STRUCT, len(self.func_stats))
      for iter13 in self.func_stats:
        iter13.write(oprot)
      oprot.writeListEnd()
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.func_stats is None:
      raise TProtocol.TProtocolException(message='Required field func_stats is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.func_stats)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class Stats_Req:

  thrift_spec = (
  )

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('Stats_Req')
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __hash__(self):
    value = 17
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class SaveSnapshot_Req:
  """
  Attributes:
   - filepath
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRING, 'filepath', None, None, ), # 1
  )

  def __init__(self, filepath=None,):
    self.filepath = filepath

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRING:
          self.filepath = iprot.readString();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('SaveSnapshot_Req')
    if self.filepath is not None:
      oprot.writeFieldBegin('filepath', TType.STRING, 1)
      oprot.writeString(self.filepath)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.filepath is None:
      raise TProtocol.TProtocolException(message='Required field filepath is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.filepath)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class ProfilerRequest:
  """
  Attributes:
   - id
   - ystats
   - save_snapshot
  """

  thrift_spec = (
    None, # 0
    (1, TType.I32, 'id', None, None, ), # 1
    (2, TType.STRUCT, 'ystats', (Stats_Req, Stats_Req.thrift_spec), None, ), # 2
    (3, TType.STRUCT, 'save_snapshot', (SaveSnapshot_Req, SaveSnapshot_Req.thrift_spec), None, ), # 3
  )

  def __init__(self, id=None, ystats=None, save_snapshot=None,):
    self.id = id
    self.ystats = ystats
    self.save_snapshot = save_snapshot

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.I32:
          self.id = iprot.readI32();
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.STRUCT:
          self.ystats = Stats_Req()
          self.ystats.read(iprot)
        else:
          iprot.skip(ftype)
      elif fid == 3:
        if ftype == TType.STRUCT:
          self.save_snapshot = SaveSnapshot_Req()
          self.save_snapshot.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('ProfilerRequest')
    if self.id is not None:
      oprot.writeFieldBegin('id', TType.I32, 1)
      oprot.writeI32(self.id)
      oprot.writeFieldEnd()
    if self.ystats is not None:
      oprot.writeFieldBegin('ystats', TType.STRUCT, 2)
      self.ystats.write(oprot)
      oprot.writeFieldEnd()
    if self.save_snapshot is not None:
      oprot.writeFieldBegin('save_snapshot', TType.STRUCT, 3)
      self.save_snapshot.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.id is None:
      raise TProtocol.TProtocolException(message='Required field id is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.id)
    value = (value * 31) ^ hash(self.ystats)
    value = (value * 31) ^ hash(self.save_snapshot)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class ProfilerResponse:
  """
  Attributes:
   - id
   - ystats
   - snapshot_filepath
  """

  thrift_spec = (
    None, # 0
    (1, TType.I32, 'id', None, None, ), # 1
    (2, TType.STRUCT, 'ystats', (Stats, Stats.thrift_spec), None, ), # 2
    (3, TType.STRING, 'snapshot_filepath', None, None, ), # 3
  )

  def __init__(self, id=None, ystats=None, snapshot_filepath=None,):
    self.id = id
    self.ystats = ystats
    self.snapshot_filepath = snapshot_filepath

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.I32:
          self.id = iprot.readI32();
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.STRUCT:
          self.ystats = Stats()
          self.ystats.read(iprot)
        else:
          iprot.skip(ftype)
      elif fid == 3:
        if ftype == TType.STRING:
          self.snapshot_filepath = iprot.readString();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('ProfilerResponse')
    if self.id is not None:
      oprot.writeFieldBegin('id', TType.I32, 1)
      oprot.writeI32(self.id)
      oprot.writeFieldEnd()
    if self.ystats is not None:
      oprot.writeFieldBegin('ystats', TType.STRUCT, 2)
      self.ystats.write(oprot)
      oprot.writeFieldEnd()
    if self.snapshot_filepath is not None:
      oprot.writeFieldBegin('snapshot_filepath', TType.STRING, 3)
      oprot.writeString(self.snapshot_filepath)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.id is None:
      raise TProtocol.TProtocolException(message='Required field id is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.id)
    value = (value * 31) ^ hash(self.ystats)
    value = (value * 31) ^ hash(self.snapshot_filepath)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)
