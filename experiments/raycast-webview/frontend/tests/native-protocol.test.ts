import { describe, expect, it } from 'vitest'
import { decodeReply } from '../app/utils/native-protocol'
const row={id:'native-token',label:'Installed app',suffix:'',type:'Application',icon:''}
const packet=(data: any={items:[row]})=>({v:2,sessionId:'session',requestId:'1',type:'search.query',status:'ok',data})
describe('native reply boundary',()=>{
  it('accepts only typed bounded native rows',()=>expect(decodeReply(JSON.stringify(packet()),'session')?.data?.items).toEqual([row]))
  it.each([null, 'not-json', 'x'.repeat(2097153), JSON.stringify({...packet(),sessionId:'old'}),JSON.stringify({...packet(),extra:true}),JSON.stringify({...packet(),requestId:1})])('rejects malformed or foreign packets',raw=>expect(decodeReply(raw,'session')).toBeUndefined())
  it.each([{...row,icon:'https://remote/icon.png'},{...row,icon:'data:image/svg+xml,<svg/>'},{...row,label:'x'.repeat(257)},{...row,id:'content://private'},{...row,extra:'field'}])('rejects unsafe row data',r=>expect(decodeReply(JSON.stringify(packet({items:[r]})),'session')).toBeUndefined())
  it('rejects duplicate identities and oversized row lists',()=>{
    expect(decodeReply(JSON.stringify(packet({items:[row,row]})),'session')).toBeUndefined()
    expect(decodeReply(JSON.stringify(packet({items:Array(101).fill(row)})),'session')).toBeUndefined()
  })
  it('validates selection and effect acknowledgements instead of trusting any success',()=>{
    const r={v:2,sessionId:'session',requestId:'2',type:'result.inspect',status:'ok',data:{selected:'native-token'}}
    expect(decodeReply(JSON.stringify(r),'session')).toBeDefined()
    expect(decodeReply(JSON.stringify({...r,data:{}}),'session')).toBeUndefined()
    expect(decodeReply(JSON.stringify({...r,type:'action.execute',data:{state:'requested'}}),'session')).toBeDefined()
    expect(decodeReply(JSON.stringify({...r,type:'action.execute',data:{state:'succeeded'}}),'session')).toBeUndefined()
    expect(decodeReply(JSON.stringify({...r,type:'unknown',data:{}}),'session')).toBeUndefined()
  })
  it('accepts machine errors but rejects leaked/unknown error shapes',()=>{
    const error={v:2,sessionId:'session',requestId:'1',type:'bridge.error',status:'error',error:{code:'CANCELLED',message:'Search cancelled'}}
    expect(decodeReply(JSON.stringify(error),'session')?.error?.code).toBe('CANCELLED')
    expect(decodeReply(JSON.stringify({...error,error:{...error.error,stack:'private'}}),'session')).toBeUndefined()
    expect(decodeReply(JSON.stringify({...error,error:{code:'SQL_EXCEPTION',message:'private'}}),'session')).toBeUndefined()
  })
})
